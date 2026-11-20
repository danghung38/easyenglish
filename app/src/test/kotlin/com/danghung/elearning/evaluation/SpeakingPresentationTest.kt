package com.danghung.elearning.evaluation

import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.QuestionRes
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class SpeakingPresentationTest {
    @Test fun promptsAreGroupedByPartAndOrderedInsteadOfTakingFirstOnly() {
        fun question(id: Long, section: String) = QuestionRes(id, 276, "SPEAKING", false,
            "Question $id", null, null, null, null, null, section, null, null)
        val groups = SpeakingParts.group(listOf(question(7, "part3"), question(2, "part1"),
            question(4, "part2"), question(1, "part1"), question(6, "part3")))
        assertEquals(listOf(listOf(1L, 2L), listOf(4L), listOf(6L, 7L)), groups.map { part -> part.map { it.id } })
    }

    @Test fun feedbackSchemaMapsAllCriteriaAndActionsIntoUi() {
        val result = Gson().fromJson("""{
          "overall_band":7.5,"score_status":"FINAL","pronunciation_status":"EXPERIMENTAL",
          "criteria_scores":{"fluency_coherence":7.5,"lexical_resource":8,"grammatical_range_accuracy":7.5,"pronunciation":7},
          "feedback_status":"COMPLETED","feedback":{"criterion_feedback":{"fluency_coherence":"FC feedback","lexical_resource":"LR feedback","grammatical_range_accuracy":"GRA feedback","pronunciation":"P feedback"},"next_actions":["Practice"]}
        }""", IELTSSpeakingRes::class.java)
        val text = SpeakingResultFormatter.criteria(result)
        listOf("FC feedback", "LR feedback", "GRA feedback", "P feedback").forEach { assertTrue(text.contains(it)) }
        assertEquals("Practice", result.feedback?.nextActions?.first())
        assertEquals("", SpeakingResultFormatter.notice(result))
        assertEquals("7.5", SpeakingResultFormatter.band(result.overallBand))
    }

    @Test fun unavailableScoresNeverRenderAsZero() {
        assertEquals("--", SpeakingResultFormatter.band(null))
        assertFalse(SpeakingResultFormatter.criteria(null).contains("0.0"))
    }

    @Test fun reviewerFailureDoesNotPretendReviewIsStillRunning() {
        val result = Gson().fromJson("""{"score_status":"PROVISIONAL","review_error":{"code":"ConnectionError"}}""", IELTSSpeakingRes::class.java)
        val notice = SpeakingResultFormatter.notice(result)
        assertTrue(notice.contains("lỗi kết nối"))
        assertFalse(notice.contains("đang kiểm tra"))
    }

    @Test fun onlyPendingOrFailedFeedbackShowsStatusText() {
        fun result(status: String) = Gson().fromJson(
            """{"score_status":"FINAL","feedback_status":"$status"}""", IELTSSpeakingRes::class.java)
        assertEquals("", SpeakingResultFormatter.notice(result("NOT_REQUESTED")))
        assertEquals("", SpeakingResultFormatter.notice(result("COMPLETED")))
        assertEquals("Đang tạo nhận xét...", SpeakingResultFormatter.notice(result("GENERATING")))
        assertTrue(SpeakingResultFormatter.notice(result("FAILED")).contains("có thể thử lại"))
    }
}
