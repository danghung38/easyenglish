package com.danghung.elearning.api.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiEvaluationV2ContractTest {

    @Test
    fun validResponse_deserializesAndIgnoresUnknownOptionalFields() {
        val value = AiEvaluationV2Contract.parse(
            validJson().replace("\"versions\":", "\"futureField\":42,\"versions\":")
        )

        assertEquals("2.0", value.schemaVersion)
        assertEquals(6.5, value.taskBand!!, 0.0)
        assertEquals(4, value.criteria!!.size)
    }

    @Test
    fun missingRequiredField_failsContract() {
        val json = validJson().replace("\"requestId\":\"req-1\",", "")

        assertThrows(IllegalArgumentException::class.java) {
            AiEvaluationV2Contract.parse(json)
        }
    }

    @Test
    fun schemaVersionMismatch_failsContract() {
        val json = validJson().replace("\"2.0\"", "\"3.0\"")

        assertThrows(IllegalArgumentException::class.java) {
            AiEvaluationV2Contract.parse(json)
        }
    }

    private fun validJson() = """
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
    """.trimIndent()
}
