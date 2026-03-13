package com.dxh.Elearning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to submit Writing exam with AI evaluation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitWritingExamRequest {
    
    /**
     * UserExamPart ID for Writing section
     */
    private Long userExamPartId;
    
    /**
     * Overall Writing band score (average of all tasks)
     */
    private Double overallBand;
    
    /**
     * List of individual task results
     */
    private List<WritingTaskResult> taskResults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WritingTaskResult {
        /**
         * Question ID (Task 1 or Task 2)
         */
        private Long questionId;
        
        /**
         * Student's essay text
         */
        private String essayText;
        
        /**
         * Word count
         */
        private Integer wordCount;
        
        /**
         * Time spent in seconds
         */
        private Integer duration;
        
        /**
         * Overall band for this task
         */
        private Double overallBand;
        
        /**
         * Task Achievement score
         */
        private Double taskAchievement;
        
        /**
         * Coherence & Cohesion score
         */
        private Double coherenceCohesion;
        
        /**
         * Lexical Resource score
         */
        private Double lexicalResource;
        
        /**
         * Grammatical Range score
         */
        private Double grammaticalRange;
        
        /**
         * Detailed feedback in Vietnamese
         */
        private String detailedFeedback;
        
        /**
         * Examiner feedback summary
         */
        private String examinerFeedback;
    }
}
