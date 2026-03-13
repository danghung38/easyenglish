package com.dxh.Elearning.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateQuestionListeningRequest {
    @NotBlank(message = "INVALID_BLANK")
    String content;

    String explain; // optional, giải thích đáp án

    Double maxScore;

    @NotNull(message = "INVALID_NULL")
    List<OptionRequest> options;

    @NotBlank(message = "INVALID_BLANK")
    String correctTempId;
}
