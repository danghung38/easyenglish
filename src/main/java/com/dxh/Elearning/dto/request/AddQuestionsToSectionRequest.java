package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.SectionName;
import jakarta.validation.Valid;
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
public class AddQuestionsToSectionRequest {
    
    @NotNull(message = "Exam Part ID is required")
    Long examPartId;
    
    @NotNull(message = "Section Name is required")
    SectionName sectionName; // SECTION1, SECTION2, SECTION3, SECTION4, SECTION5
    
    String sectionContent; // Optional instructions (e.g., "Questions 1-10: Complete the notes below")
    
    String transcript; // Optional transcript for listening section audio
    
    @NotEmpty(message = "Questions list cannot be empty")
    @Valid
    List<QuestionInSectionRequest> questions; // List of questions to add to this section
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestionInSectionRequest {
        @NotNull(message = "Question content is required")
        String content;
        
        @NotNull(message = "Question type is required")
        String type; // MULTIPLE_CHOICE (includes fill-in-blank and short answer)
        
        String correctAnswer;
        
        Integer maxScore;
        
        List<String> options; // For multiple choice questions
    }
}
