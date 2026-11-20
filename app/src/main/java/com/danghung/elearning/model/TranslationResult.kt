package com.danghung.elearning.model

data class MyMemoryResponse(
    val responseData: ResponseData,
    val responseStatus: Int,
    val responseDetails: String?,
    val matches: List<TranslationMatch>?
)

data class ResponseData(
    val translatedText: String,
    val match: Float
)

data class TranslationMatch(
    val id: String?,
    val segment: String,
    val translation: String,
    val quality: String?,
    val reference: String?,
    val subject: String?
)