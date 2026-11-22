package com.dxh.Elearning.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitRLPartRequest {

    @NotNull(message = "INVALID_NULL")
    Long userExamPartId;

    List<AnswerRLPartRequest> answers;
}
