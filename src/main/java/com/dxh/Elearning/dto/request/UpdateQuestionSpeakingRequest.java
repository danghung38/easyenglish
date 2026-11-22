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
public class UpdateQuestionSpeakingRequest {
    @NotBlank(message = "INVALID_BLANK")
    String content;

    @NotNull(message = "INVALID_NULL")
    Double maxScore;
    
    String explain; // Giải thích đáp án
    
    /**
     * Speaking part: "part1" (Interview), "part2" (Long Turn), "part3" (Discussion)
     */
    String section;
}
