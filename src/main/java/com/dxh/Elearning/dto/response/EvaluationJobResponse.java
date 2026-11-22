package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluationJobResponse {
    private String jobId;
    private String requestId;
    private String idempotencyKey;
    private String skill;
    private String status;
    private Integer attemptCount;
    private Integer progress;
    private Map<String, Object> result;
    private String errorCode;
    private String errorMessage;
    private String createdAt;
    private String updatedAt;
}
