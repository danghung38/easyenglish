package com.dxh.Elearning.dto.response.ai.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiEvaluationResultV2 {
    private String schemaVersion;
    private String status;
    private String requestId;
    private String idempotencyKey;
    private String skill;
    private String task;
    private Map<String, CriterionResultV2> criteria;
    private Double taskBand;
    private Double overallBand;
    private Double confidence;
    private ReviewResultV2 review;
    private List<String> strengths;
    private List<String> priorityImprovements;
    private List<Map<String, Object>> evidence;
    private VersionSnapshotV2 versions;
}
