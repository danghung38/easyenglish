package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Response from IELTSensei Speaking evaluation
 * Based on IELTS Speaking band descriptors (0-9)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IELTSSpeakingResponse {
    
    /**
     * Overall IELTS band score (0-9, rounded to nearest 0.5)
     */
    @JsonProperty("overall_band")
    private Double overallBand;
    
    /**
     * Individual criteria scores
     */
    @JsonProperty("criteria_scores")
    private CriteriaScores criteriaScores;
    
    /**
     * Detailed feedback for each criterion (can be String or Object)
     * Flask API returns this as String, so we store it as String
     */
    @JsonProperty("detailed_feedback")
    private String detailedFeedback;
    
    /**
     * Examiner's summary feedback
     */
    @JsonProperty("examiner_feedback")
    private String examinerFeedback;
    
    /**
     * List of strengths
     */
    private List<String> strengths;
    
    /**
     * Areas that need improvement
     */
    @JsonProperty("areas_for_improvement")
    private List<String> areasForImprovement;
    
    /**
     * Timestamp of evaluation
     */
    private String timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CriteriaScores {
        @JsonProperty("fluency_coherence")
        private Double fluencyCoherence;
        
        @JsonProperty("lexical_resource")
        private Double lexicalResource;
        
        @JsonProperty("grammatical_range_accuracy")
        private Double grammaticalRange;
        
        private Double pronunciation;
    }
}
