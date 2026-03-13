package com.dxh.Elearning.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request to submit the entire Speaking exam (all 3 parts) and update final score
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitSpeakingExamRequest {
    
    @NotNull(message = "User exam part ID is required")
    Long userExamPartId;
    
    @NotNull(message = "Overall band score is required")
    Double overallBand;
    
    // Optional: individual part scores
    Double part1Score;
    Double part2Score;
    Double part3Score;
    
    // Optional: detailed criteria scores
    Double fluencyCoherence;
    Double lexicalResource;
    Double grammaticalRange;
    Double pronunciation;
    
    // Optional: overall feedback
    String overallFeedback;
}
