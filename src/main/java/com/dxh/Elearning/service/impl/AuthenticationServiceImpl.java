package com.dxh.Elearning.service.impl;


import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.AuthenticationResponse;
import com.dxh.Elearning.dto.response.IntrospectResponse;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.enums.Role;
import com.dxh.Elearning.enums.TokenType;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.RoleRepository;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.service.interfac.AuthenticationService;
import com.dxh.Elearning.service.interfac.TwoFactorService;
import com.dxh.Elearning.service.LoginAttemptService;
import com.dxh.Elearning.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    LoginAttemptService loginAttemptService;
    TokenBlacklistService tokenBlacklistService;
    TwoFactorService twoFactorService;


    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${google.client-id}")
    protected String GOOGLE_CLIENT_ID;

    @NonFinal
    @Value("${facebook.app-id}")
    protected String FACEBOOK_APP_ID;

    @NonFinal
    @Value("${facebook.app-secret}")
    protected String FACEBOOK_APP_SECRET;


    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {

        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, TokenType.ACCESS);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }


//    login
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request, String ip){
        loginAttemptService.assertNotBlocked(request.getUsername(), ip);

        var user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByUsername(request.getUsername()))
                .orElse(null);
        boolean authenticated = user != null
                && user.getPassword() != null
                && passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            loginAttemptService.loginFailed(request.getUsername(), ip);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        loginAttemptService.loginSucceeded(request.getUsername(), ip);
        return createLoginResponse(user);
    }


    @Override
    @Transactional
    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) {
        // 1. Verify idToken với Google
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

        String googleId = payload.getSubject();
        String email    = payload.getEmail();
        String fullName = (String) payload.get("name");
        String avatar   = (String) payload.get("picture");

        // 2. Tìm user theo googleId → email → tạo mới
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> {
                            // Email đã tồn tại → liên kết googleId vào tài khoản cũ
                            existing.setGoogleId(googleId);
                            if (existing.getAvatar() == null && avatar != null)
                                existing.setAvatar(avatar);
                            existing.setEnabled(true);
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> createGoogleUser(googleId, email, fullName, avatar))
                );

        // 3. Kiểm tra tài khoản có bị khóa không
        if (!user.getEnabled())
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);

        // 4. Tạo JWT app và trả về
        return createLoginResponse(user);
    }

    private AuthenticationResponse createLoginResponse(User user) {
        String roles = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.joining(" "));

        if (Boolean.TRUE.equals(user.getIs2faEnabled())) {
            return AuthenticationResponse.builder()
                    .authenticated(false)
                    .requires2fa(true)
                    .tempToken(generateToken(user, TokenType.TWO_FACTOR_PENDING))
                    .role(roles)
                    .fullName(user.getFullName())
                    .username(user.getUsername())
                    .build();
        }

        return issueTokenResponse(user, roles);
    }

    private AuthenticationResponse issueTokenResponse(User user, String roles) {
        return AuthenticationResponse.builder()
                .authenticated(true)
                .requires2fa(false)
                .accessToken(generateToken(user, TokenType.ACCESS))
                .refreshToken(generateToken(user, TokenType.REFRESH))
                .role(roles)
                .fullName(user.getFullName())
                .username(user.getUsername())
                .build();
    }

    @Override
    public AuthenticationResponse authenticateTwoFactor(TwoFactorLoginRequest request)
            throws ParseException, JOSEException {
        SignedJWT pendingToken = verifyToken(request.getTempToken(), TokenType.TWO_FACTOR_PENDING);
        JWTClaimsSet claims = pendingToken.getJWTClaimsSet();
        if (!"2FA_PENDING".equals(claims.getStringClaim("scope"))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findByUsername(claims.getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (!Boolean.TRUE.equals(user.getIs2faEnabled())
                || !twoFactorService.verifyCode(user.getTotpSecret(), request.getCode())) {
            throw new AppException(ErrorCode.INVALID_2FA_CODE);
        }

        tokenBlacklistService.blacklist(
                claims.getJWTID(),
                claims.getExpirationTime());

        String roles = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.joining(" "));
        return issueTokenResponse(user, roles);
    }

    private User createGoogleUser(String googleId, String email, String fullName, String avatar) {
        // Tạo username unique từ phần trước @ của email
        String base = email.split("@")[0];
        String username = base;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + i++;
        }

        User user = User.builder()
                .googleId(googleId)
                .email(email)
                .fullName(fullName != null ? fullName : username)
                .username(username)
                .avatar(avatar)
                .enabled(true)       // Google đã verify email rồi, bật luôn
                .bandsTarget(9.0)
                // password = null vì login bằng Google
                .roles(Set.of(roleRepository.findByName(Role.USER.name())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED))))
                .build();

        return userRepository.save(user);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null)
                throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);

            return idToken.getPayload();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }


    private String generateToken(User user, TokenType tokenType) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Instant expiration = tokenType == TokenType.TWO_FACTOR_PENDING
                ? Instant.now().plus(5, ChronoUnit.MINUTES)
                : Instant.now().plus(
                tokenType == TokenType.ACCESS ? VALID_DURATION : REFRESHABLE_DURATION,
                ChronoUnit.HOURS);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("danghung.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        expiration.toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("token_type", tokenType.name())
                .claim("scope", tokenType == TokenType.ACCESS
                        ? buildScope(user)
                        : tokenType == TokenType.TWO_FACTOR_PENDING ? "2FA_PENDING" : "")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    //build roles
    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_"+role.getName());

                //duyệt list permission trong roles
                if(!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }


        return stringJoiner.toString();
    }


    //token sắp hết hạn thì gia hạn
    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), TokenType.REFRESH);

        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        //lưu vào bảng token đã logout coi như hết hạn

        //tìm user để tạo token
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED)
        );

        //tạo token
        tokenBlacklistService.blacklist(jti, expiryTime);

        return AuthenticationResponse.builder()
                .accessToken(generateToken(user, TokenType.ACCESS))
                .refreshToken(generateToken(user, TokenType.REFRESH))
                .authenticated(true)
                .build();
    }


    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getAccessToken(), TokenType.ACCESS);

            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            tokenBlacklistService.blacklist(jti, expiryTime);

        } catch (AppException exception){
            log.debug("Access token was already invalid during logout");
        }

        try {
            var signToken = verifyToken(request.getRefreshToken(), TokenType.REFRESH);
            tokenBlacklistService.blacklist(
                    signToken.getJWTClaimsSet().getJWTID(),
                    signToken.getJWTClaimsSet().getExpirationTime());
        } catch (AppException exception) {
            log.debug("Refresh token was already invalid during logout");
        }
    }



    private SignedJWT verifyToken(String token, TokenType expectedType) throws JOSEException, ParseException {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!JWSAlgorithm.HS512.equals(signedJWT.getHeader().getAlgorithm())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date expiryTime = claims.getExpirationTime();
        if (expiryTime == null || !expiryTime.after(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!"danghung.com".equals(claims.getIssuer())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenTypeClaim = claims.getStringClaim("token_type");
        if (!expectedType.name().equals(tokenTypeClaim)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (expectedType == TokenType.TWO_FACTOR_PENDING
                && !"2FA_PENDING".equals(claims.getStringClaim("scope"))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String jti = claims.getJWTID();
        if (jti == null || jti.isBlank() || tokenBlacklistService.isBlacklisted(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}

