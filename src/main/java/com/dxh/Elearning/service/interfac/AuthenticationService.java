package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.AuthenticationResponse;
import com.dxh.Elearning.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    //    kiểm tra token
    IntrospectResponse introspect(IntrospectRequest request)
                throws JOSEException, ParseException;

    //đăng nhập và tạo token
    AuthenticationResponse authenticate(AuthenticationRequest request, String ip);

    //token sắp hết hạn thì gia hạn
    AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    AuthenticationResponse loginWithGoogle(GoogleLoginRequest request);

    AuthenticationResponse authenticateTwoFactor(TwoFactorLoginRequest request)
            throws ParseException, JOSEException;
}
