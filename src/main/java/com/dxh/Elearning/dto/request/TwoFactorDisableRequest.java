package com.dxh.Elearning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TwoFactorDisableRequest {
    @NotBlank
    String password;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    String code;
}
