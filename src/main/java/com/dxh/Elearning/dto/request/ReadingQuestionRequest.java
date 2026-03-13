package com.dxh.Elearning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingQuestionRequest {
    
    String content; // Nội dung câu hỏi (optional - có thể để trống)
    
    @NotNull(message = "Max score is required")
    Double maxScore; // Điểm tối đa
    
    @NotNull(message = "Options are required")
    List<OptionRequest> options; // 4 options: A, B, C, D
    
    @NotBlank(message = "Correct answer is required")
    String correctTempId; // "A", "B", "C", hoặc "D"
}
