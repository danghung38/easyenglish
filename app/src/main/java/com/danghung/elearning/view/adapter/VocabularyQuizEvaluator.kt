package com.danghung.elearning.view.adapter

internal object VocabularyQuizEvaluator {
    fun isCorrect(selectedAnswer: String?, correctAnswer: String?): Boolean {
        if (selectedAnswer.isNullOrBlank() || correctAnswer.isNullOrBlank()) return false
        return selectedAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
    }
}
