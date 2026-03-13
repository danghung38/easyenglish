package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.SectionName;
import com.dxh.Elearning.enums.SkillType;
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
public class SectionRequest {
    
    @NotNull(message = "Exam Part ID is required")
    Long examPartId;
    
    @NotNull(message = "Skill Type is required")
    SkillType skillType; // LISTENING
    
    @NotNull(message = "Section Name is required")
    SectionName sectionName; // SECTION1, SECTION2, SECTION3, SECTION4, SECTION5
    
    String sectionContent; // Optional instructions (e.g., "Questions 1-10 Complete the notes below")
    
    String transcript; // Optional transcript for listening section audio
    
    @NotEmpty(message = "Question IDs list cannot be empty")
    List<Long> questionIds; // Required: List of existing question IDs to assign to this section
    
    // Helper method to get display name
    public String getSectionDisplayName() {
        return sectionName != null ? sectionName.getDisplayName() : null;
    }
}
