package com.danghung.elearning.api.res

import java.io.Serializable

data class TopicRes(
    val title: String,
    val wordCount: Int
): Serializable