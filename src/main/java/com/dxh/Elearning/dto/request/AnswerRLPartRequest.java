package com.dxh.Elearning.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRLPartRequest {

    @NotNull(message = "INVALID_NULL")
    Long questionId;

    @NotNull(message = "INVALID_NULL")
    Long selectedOptionId;
}
