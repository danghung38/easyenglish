package com.dxh.Elearning.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateQuestionWritingRequest {
    @NotBlank(message = "INVALID_BLANK")
    String content;

    @NotNull(message = "INVALID_NULL")
    Double maxScore;
    
    String explain; // Giải thích đáp án
    
    /**
     * Writing task type: "task1" (Report/Chart), "task2" (Essay)
     */
    String section;
    
    /**
     * Image URL for Writing Task 1 (charts, graphs, diagrams)
     */
    String imageUrl;
}
