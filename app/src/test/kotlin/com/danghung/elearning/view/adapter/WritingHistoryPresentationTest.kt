package com.danghung.elearning.view.adapter

import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.UserAnswerRes
import org.junit.Assert.assertEquals
import org.junit.Test

class WritingHistoryPresentationTest {

    @Test
    fun `task order keeps task one before task two`() {
        assertEquals(1, WritingHistoryPresentation.taskOrder("task1"))
        assertEquals(2, WritingHistoryPresentation.taskOrder("TASK2"))
        assertEquals(3, WritingHistoryPresentation.taskOrder(null))
    }

    @Test
    fun `history preserves both writing tasks and orders them correctly`() {
        val taskTwo = answer(id = 2, section = "task2")
        val taskOne = answer(id = 1, section = "task1")

        val ordered = WritingHistoryPresentation.sortAnswers(listOf(taskTwo, taskOne))

        assertEquals(2, ordered.size)
        assertEquals(listOf("task1", "task2"), ordered.map { it.question?.section })
    }

    @Test
    fun `band badge is compact and stays on one line`() {
        assertEquals("AI Band 6.0", WritingHistoryPresentation.bandLabel(6.0))
        assertEquals("AI Band -", WritingHistoryPresentation.bandLabel(null))
    }

    @Test
    fun `fallback score averages all four criteria on half band`() {
        val feedback = WritingHistoryAdapter.WritingFeedback(
            taskAchievement = 6.0,
            coherenceCohesion = 5.5,
            lexicalResource = 5.0,
            grammaticalRange = 5.0,
            detailedFeedback = null,
            examinerFeedback = null,
            confidence = null,
            strengths = null,
            areasForImprovement = null,
            evidence = null,
            review = null
        )

        assertEquals(5.5, WritingHistoryPresentation.computeScore(feedback) ?: 0.0, 0.0)
    }

    private fun answer(id: Long, section: String): UserAnswerRes = UserAnswerRes(
        id = id,
        question = QuestionRes(
            id = id,
            examPartId = 1,
            skillType = "WRITING",
            isSection = false,
            content = "Question $id",
            audioUrl = null,
            imageUrl = null,
            maxScore = null,
            correctOptionId = null,
            optionRes = null,
            section = section,
            explain = null,
            transcript = null
        ),
        selectedOptionId = null,
        answerText = "Answer $id",
        audioUrl = null,
        score = null,
        aiScore = 5.0,
        aiFeedback = null
    )
}
