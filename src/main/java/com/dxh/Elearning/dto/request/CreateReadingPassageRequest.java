package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReadingPassageRequest {
    
    @NotNull(message = "Exam Part ID is required")
    Long examPartId;
    
    @NotBlank(message = "Section name is required")
    String section; // Section name for grouping questions (e.g., "Section 1", "Reading Passage 1")
    
    @NotEmpty(message = "Questions list cannot be empty")
    List<ReadingQuestionRequest> questions; // List of questions for this passage
}
