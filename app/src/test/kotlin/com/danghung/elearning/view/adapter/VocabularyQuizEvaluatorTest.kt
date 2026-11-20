package com.danghung.elearning.view.adapter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyQuizEvaluatorTest {
    @Test
    fun acceptsSameAnswerIgnoringCaseAndWhitespace() {
        assertTrue(VocabularyQuizEvaluator.isCorrect(" Resilient ", "resilient"))
    }

    @Test
    fun rejectsWrongBlankAndMissingAnswers() {
        assertFalse(VocabularyQuizEvaluator.isCorrect("fragile", "resilient"))
        assertFalse(VocabularyQuizEvaluator.isCorrect("", "resilient"))
        assertFalse(VocabularyQuizEvaluator.isCorrect(null, "resilient"))
        assertFalse(VocabularyQuizEvaluator.isCorrect("resilient", null))
    }
}
