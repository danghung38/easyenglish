package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRLPartResponse {
    Long id;

    Long selectedOptionId;

    Double score;  // Score for this answer (1.0 = correct, 0.0 = incorrect)

    QuestionResponse questionResponse;
}
