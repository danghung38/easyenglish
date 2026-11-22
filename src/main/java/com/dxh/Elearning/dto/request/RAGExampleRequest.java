package com.dxh.Elearning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGExampleRequest {
    private String text;
    private EvaluationData evaluation;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationData {
        private Double overallBand;
        private Map<String, Double> criteriaScores;
        private Object detailedFeedback;  // String or Map depending on type
        private String examinerFeedback;
        private List<String> strengths;
        private List<String> areasForImprovement;
    }
}
