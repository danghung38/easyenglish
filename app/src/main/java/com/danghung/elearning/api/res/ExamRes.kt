package com.danghung.elearning.api.res

import java.io.Serializable

data class ExamRes(
    val id: Long,
    val title: String,
    val description: String?,
    val totalDuration: Int,
    val imageUrl: String?,
    val examParts: List<ExamPartRes>
) : Serializable