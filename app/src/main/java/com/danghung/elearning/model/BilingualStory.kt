package com.danghung.elearning.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.danghung.elearning.R

data class BilingualStory(
    val id: Long,
    val title: String,
    val titleVi: String,
    val summary: String?,
    val imageUrl: String?,
    val level: StoryLevel
)

enum class StoryLevel(
    @StringRes val label: Int,
    @ColorRes val surface: Int,
    @ColorRes val ink: Int
) {
    BEGINNER(R.string.txt_story_beginner, R.color.story_beginner_surface, R.color.story_beginner_ink),
    INTERMEDIATE(R.string.txt_story_intermediate, R.color.story_intermediate_surface, R.color.story_intermediate_ink),
    ADVANCED(R.string.txt_story_advanced, R.color.story_advanced_surface, R.color.story_advanced_ink)
}
