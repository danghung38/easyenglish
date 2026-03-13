package com.danghung.elearning.api.res

import java.io.Serializable

data class VocabularyRes(
    val id: Int,
    val word: String,
    val meaning: String,
    val pronunciation: String,
    val pronunciationAudioUrl: String,
    val example: String,
    val imageUrl: String,
    val topic: String
): Serializable