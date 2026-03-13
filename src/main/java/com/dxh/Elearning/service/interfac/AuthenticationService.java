package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.AuthenticationRequest;
import com.dxh.Elearning.dto.request.IntrospectRequest;
import com.dxh.Elearning.dto.request.LogoutRequest;
import com.dxh.Elearning.dto.request.RefreshRequest;
import com.dxh.Elearning.dto.response.AuthenticationResponse;
import com.dxh.Elearning.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    //    kiểm tra token
    IntrospectResponse introspect(IntrospectRequest request)
                throws JOSEException, ParseException;

    //đăng nhập và tạo token
    AuthenticationResponse authenticate(AuthenticationRequest request);

    //token sắp hết hạn thì gia hạn
    AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;
}
