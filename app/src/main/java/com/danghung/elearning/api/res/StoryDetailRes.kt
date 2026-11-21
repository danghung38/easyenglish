package com.danghung.elearning.api.res

import com.danghung.elearning.model.BilingualStory

data class StoryDetailRes(
    val story: BilingualStory,
    val content: String,
    val translation: String
)
