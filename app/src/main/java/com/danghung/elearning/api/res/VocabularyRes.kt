package com.danghung.elearning.api.res

import java.io.Serializable

data class VocabularyRes(
    val id: Int,
    val word: String,
    val meaning: String,
    val pronunciation: String?,
    val pronunciationAudioUrl: String?,
    val example: String?,
    val imageUrl: String?,
    val topic: String,
    val chatbotQuiz: Boolean? = false,
    val quizQuestion: String? = null,
    val quizOptions: List<String>? = null,
    val correctAnswer: String? = null,
    val explanation: String? = null
): Serializable
