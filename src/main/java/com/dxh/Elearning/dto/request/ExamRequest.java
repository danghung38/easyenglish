package com.dxh.Elearning.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamRequest {

    @NotBlank(message = "INVALID_BLANK")
    String title;

    @NotBlank(message = "INVALID_BLANK")
    String description;

    @NotNull(message = "INVALID_NULL")
    Integer totalDuration; // phút
}
