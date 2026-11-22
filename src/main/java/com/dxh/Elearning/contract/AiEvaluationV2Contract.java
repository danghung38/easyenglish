package com.dxh.Elearning.contract;

import com.dxh.Elearning.dto.response.ai.v2.AiEvaluationResultV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public final class AiEvaluationV2Contract {

    public static final String SCHEMA_VERSION = "2.0";

    private AiEvaluationV2Contract() {
    }

    public static AiEvaluationResultV2 parse(ObjectMapper mapper, String json)
            throws JsonProcessingException {
        AiEvaluationResultV2 result = mapper.readValue(json, AiEvaluationResultV2.class);
        validate(result);
        return result;
    }

    public static void validate(AiEvaluationResultV2 result) {
        require(result, "result");
        if (!SCHEMA_VERSION.equals(result.getSchemaVersion())) {
            throw new IllegalArgumentException(
                    "Unsupported AI schema version: " + result.getSchemaVersion()
            );
        }
        require(result.getStatus(), "status");
        require(result.getRequestId(), "requestId");
        require(result.getIdempotencyKey(), "idempotencyKey");
        require(result.getSkill(), "skill");
        require(result.getCriteria(), "criteria");
        require(result.getReview(), "review");
        require(result.getStrengths(), "strengths");
        require(result.getPriorityImprovements(), "priorityImprovements");
        require(result.getEvidence(), "evidence");
        require(result.getVersions(), "versions");
        if (result.getCriteria().isEmpty()) {
            throw new IllegalArgumentException("criteria must not be empty");
        }
        result.getCriteria().forEach((name, criterion) -> {
            require(criterion, "criteria." + name);
            require(criterion.getBand(), "criteria." + name + ".band");
            require(criterion.getEvidenceIds(), "criteria." + name + ".evidenceIds");
            require(criterion.getFeedback(), "criteria." + name + ".feedback");
        });
    }

    private static void require(Object value, String field) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
    }
}
