package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.Gender;
import com.dxh.Elearning.validator.DobConstraint;
import com.dxh.Elearning.validator.GenderSubset;
import com.dxh.Elearning.validator.PhoneNumber;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 6,message = "USERNAME_INVALID")
    String username;

    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "INVALID_NAME")
    String fullName;

    @PhoneNumber(message = "INVALID_PHONENUMBER")
    @NotBlank(message = "INVALID_BLANK")
    String phoneNumber;

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "INVALID_BLANK")
    String email;


//    @EnumValue(name = "gender", enumClass = Gender.class)
    @GenderSubset(anyOf = {Gender.MALE, Gender.FEMALE, Gender.OTHER},message = "INVALID_GENDER")
    String gender;



    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;
}