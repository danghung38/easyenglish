package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.TwoFactorCodeRequest;
import com.dxh.Elearning.dto.request.TwoFactorDisableRequest;
import com.dxh.Elearning.dto.response.TwoFactorSetupResponse;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.service.interfac.TwoFactorService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TwoFactorServiceImpl implements TwoFactorService {
    UserRepository userRepository;
    SecretGenerator secretGenerator;
    CodeVerifier codeVerifier;
    PasswordEncoder passwordEncoder;

    @Value("${app.name:EasyEnglish}")
    @NonFinal
    String appName;

    @Override
    @Transactional
    public TwoFactorSetupResponse setup() {
        User user = currentUser();
        if (Boolean.TRUE.equals(user.getIs2faEnabled())) {
            throw new AppException(ErrorCode.TWO_FACTOR_ALREADY_ENABLED);
        }

        String secret = secretGenerator.generate();
        user.setTotpSecret(secret);
        user.setIs2faEnabled(false);
        userRepository.save(user);

        return TwoFactorSetupResponse.builder()
                .secretKey(secret)
                .qrCodeUri(buildQrCodeUri(user.getEmail(), secret))
                .build();
    }

    @Override
    @Transactional
    public void verifyEnable(TwoFactorCodeRequest request) {
        User user = currentUser();
        if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
            throw new AppException(ErrorCode.TWO_FACTOR_NOT_SETUP);
        }
        if (!verifyCode(user.getTotpSecret(), request.getCode())) {
            throw new AppException(ErrorCode.INVALID_2FA_CODE);
        }

        user.setIs2faEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disable(TwoFactorDisableRequest request) {
        User user = currentUser();
        if (!Boolean.TRUE.equals(user.getIs2faEnabled()) || user.getTotpSecret() == null) {
            throw new AppException(ErrorCode.TWO_FACTOR_NOT_SETUP);
        }
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!verifyCode(user.getTotpSecret(), request.getCode())) {
            throw new AppException(ErrorCode.INVALID_2FA_CODE);
        }

        user.setIs2faEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        return secret != null && !secret.isBlank()
                && code != null && code.matches("\\d{6}")
                && codeVerifier.isValidCode(secret, code);
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String buildQrCodeUri(String email, String secret) {
        String issuer = encode(appName);
        String account = encode(appName + ":" + email);
        return "otpauth://totp/" + account
                + "?secret=" + secret
                + "&issuer=" + issuer;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
