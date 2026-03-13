package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressUpdateRequest {

    @NotBlank(message = "INVALID_BLANK")
    String fullName;

    @NotBlank(message = "INVALID_BLANK")
    @PhoneNumber(message = "INVALID_PHONENUMBER")
    String phone;

    @NotBlank(message = "INVALID_BLANK")
    String city;

    @NotBlank(message = "INVALID_BLANK")
    String district;

    @NotBlank(message = "INVALID_BLANK")
    String wards;

    @NotBlank(message = "INVALID_BLANK")
    String specificAddress;
}
