package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Response from IELTSensei Writing evaluation
 * Based on IELTS Writing band descriptors (0-9)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IELTSWritingResponse {

    private String status;
    
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
     * Detailed feedback for each criterion
     */
    @JsonProperty("detailed_feedback")
    private DetailedFeedback detailedFeedback;
    
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

    private Double confidence;

    private List<Evidence> evidence;

    private Review review;

    @JsonProperty("retrieval_document_ids")
    private List<String> retrievalDocumentIds;

    @JsonProperty("retrieval_index_version")
    private String retrievalIndexVersion;

    @JsonProperty("retrieval_audit")
    private List<Map<String, Object>> retrievalAudit;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CriteriaScores {
        @JsonProperty("task_achievement")
        private Double taskAchievement;
        
        @JsonProperty("coherence_cohesion")
        private Double coherenceCohesion;
        
        @JsonProperty("lexical_resource")
        private Double lexicalResource;
        
        @JsonProperty("grammatical_range_accuracy")
        private Double grammaticalRange;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedFeedback {
        @JsonProperty("task_achievement")
        private String taskAchievement;
        
        @JsonProperty("coherence_cohesion")
        private String coherenceCohesion;
        
        @JsonProperty("lexical_resource")
        private String lexicalResource;
        
        @JsonProperty("grammatical_range_accuracy")
        private String grammaticalRange;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evidence {
        @JsonProperty("evidence_id")
        private String evidenceId;
        private Integer start;
        private Integer end;
        private String quote;
        private String criterion;
        private String signal;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        private Boolean required;
        private Boolean performed;
        private String decision;
        @JsonProperty("reason_codes")
        private List<String> reasonCodes;
        private String rationale;
    }
}
