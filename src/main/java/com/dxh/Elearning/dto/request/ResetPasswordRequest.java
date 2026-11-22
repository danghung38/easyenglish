package com.dxh.Elearning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "INVALID_BLANK")
    String resetCode;

    @NotBlank(message = "INVALID_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String newPassword;
}