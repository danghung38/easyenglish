package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.TwoFactorCodeRequest;
import com.dxh.Elearning.dto.request.TwoFactorDisableRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.TwoFactorSetupResponse;
import com.dxh.Elearning.service.interfac.TwoFactorService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TwoFactorController {
    TwoFactorService twoFactorService;

    @PostMapping("/setup")
    ApiResponse<TwoFactorSetupResponse> setup() {
        return ApiResponse.<TwoFactorSetupResponse>builder()
                .code(HttpStatus.OK.value())
                .result(twoFactorService.setup())
                .build();
    }

    @PostMapping("/verify-enable")
    ApiResponse<Void> verifyEnable(@RequestBody @Valid TwoFactorCodeRequest request) {
        twoFactorService.verifyEnable(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Two-factor authentication enabled")
                .build();
    }

    @PostMapping("/disable")
    ApiResponse<Void> disable(@RequestBody @Valid TwoFactorDisableRequest request) {
        twoFactorService.disable(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Two-factor authentication disabled")
                .build();
    }
}
