package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.Gender;
import com.dxh.Elearning.validator.DobConstraint;
import com.dxh.Elearning.validator.GenderSubset;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @NotBlank(message = "INVALID_NAME")
    String fullName;

    //    @EnumValue(name = "gender", enumClass = Gender.class)
    @GenderSubset(anyOf = {Gender.MALE, Gender.FEMALE, Gender.OTHER},message = "INVALID_GENDER")
    String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    Double bandsTarget;
}
