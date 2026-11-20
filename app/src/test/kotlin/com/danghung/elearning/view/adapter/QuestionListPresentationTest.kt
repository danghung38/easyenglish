package com.danghung.elearning.view.adapter

import com.danghung.elearning.api.res.QuestionRes
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionListPresentationTest {

    @Test
    fun `section header is displayed before its questions even when created later`() {
        val questionTwo = question(id = 12, section = "Section 1")
        val section = question(id = 99, section = "Section 1", isSection = true)
        val questionOne = question(id = 11, section = "Section 1")

        val ordered = QuestionListPresentation.order(listOf(questionTwo, section, questionOne))

        assertEquals(listOf(99L, 11L, 12L), ordered.map { it.id })
    }

    @Test
    fun `question numbering ignores section rows`() {
        val ordered = QuestionListPresentation.order(
            listOf(
                question(id = 1, section = "Section 1", isSection = true),
                question(id = 2, section = "Section 1"),
                question(id = 3, section = "Section 1"),
                question(id = 4, section = "Section 2", isSection = true),
                question(id = 5, section = "Section 2")
            )
        )

        assertEquals(3, QuestionListPresentation.answerableCount(ordered))
        assertEquals(1, QuestionListPresentation.questionNumberAt(ordered, 1))
        assertEquals(2, QuestionListPresentation.questionNumberAt(ordered, 2))
        assertEquals(3, QuestionListPresentation.questionNumberAt(ordered, 4))
    }

    private fun question(id: Long, section: String, isSection: Boolean = false) = QuestionRes(
        id = id,
        examPartId = 1,
        skillType = "LISTENING",
        isSection = isSection,
        content = if (isSection) "Instructions" else id.toString(),
        audioUrl = null,
        imageUrl = null,
        maxScore = if (isSection) 0.0 else 1.0,
        correctOptionId = null,
        optionRes = emptyList(),
        section = section,
        explain = null,
        transcript = null
    )
}
