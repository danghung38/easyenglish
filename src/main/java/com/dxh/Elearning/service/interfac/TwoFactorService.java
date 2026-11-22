package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.TwoFactorCodeRequest;
import com.dxh.Elearning.dto.request.TwoFactorDisableRequest;
import com.dxh.Elearning.dto.response.TwoFactorSetupResponse;

public interface TwoFactorService {
    TwoFactorSetupResponse setup();

    void verifyEnable(TwoFactorCodeRequest request);

    void disable(TwoFactorDisableRequest request);

    boolean verifyCode(String secret, String code);
}
