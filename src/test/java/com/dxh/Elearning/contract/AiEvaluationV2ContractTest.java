package com.dxh.Elearning.contract;

import com.dxh.Elearning.dto.response.ai.v2.AiEvaluationResultV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiEvaluationV2ContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializesValidResponseAndIgnoresUnknownOptionalFields() throws Exception {
        AiEvaluationResultV2 result = AiEvaluationV2Contract.parse(
                mapper,
                validJson().replace("\"versions\":", "\"futureField\":42,\"versions\":")
        );

        assertEquals("2.0", result.getSchemaVersion());
        assertEquals(6.5, result.getTaskBand());
        assertEquals(4, result.getCriteria().size());
    }

    @Test
    void rejectsMissingRequiredField() {
        String json = validJson().replace("\"requestId\":\"req-1\",", "");

        assertThrows(
                IllegalArgumentException.class,
                () -> AiEvaluationV2Contract.parse(mapper, json)
        );
    }

    @Test
    void rejectsSchemaVersionMismatch() {
        String json = validJson().replace("\"2.0\"", "\"3.0\"");

        assertThrows(
                IllegalArgumentException.class,
                () -> AiEvaluationV2Contract.parse(mapper, json)
        );
    }

    private String validJson() {
        return """
                {
                  "schemaVersion":"2.0",
                  "status":"COMPLETED",
                  "requestId":"req-1",
                  "idempotencyKey":"submission-1",
                  "skill":"writing",
                  "task":"task2",
                  "criteria":{
                    "task_achievement":{"band":6.5,"confidence":null,"evidenceIds":[],"feedback":"TA"},
                    "coherence_cohesion":{"band":6.5,"confidence":null,"evidenceIds":[],"feedback":"CC"},
                    "lexical_resource":{"band":6.5,"confidence":null,"evidenceIds":[],"feedback":"LR"},
                    "grammatical_range_accuracy":{"band":6.5,"confidence":null,"evidenceIds":[],"feedback":"GRA"}
                  },
                  "taskBand":6.5,
                  "overallBand":null,
                  "confidence":null,
                  "review":{"required":false,"performed":false,"reasonCodes":[]},
                  "strengths":[],
                  "priorityImprovements":[],
                  "evidence":[],
                  "versions":{"model":"model","rubric":"rubric","prompts":"prompt","retrievalIndex":"index"}
                }
                """;
    }
}
