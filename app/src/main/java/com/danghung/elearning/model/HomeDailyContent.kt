package com.danghung.elearning.model

import androidx.annotation.StringRes
import com.danghung.elearning.R

data class HomeWord(
    @StringRes val word: Int,
    @StringRes val definition: Int,
    @StringRes val example: Int,
    @StringRes val prompt: Int
)

data class HomeDailyContent(
    val word: HomeWord,
    val answer: HomeWord,
    val choices: List<HomeWord>
) {
    companion object {
        private val words = listOf(
            HomeWord(R.string.home_word_resilient, R.string.home_definition_resilient,
                R.string.home_example_resilient, R.string.home_prompt_resilient),
            HomeWord(R.string.home_word_concise, R.string.home_definition_concise,
                R.string.home_example_concise, R.string.home_prompt_concise),
            HomeWord(R.string.home_word_plausible, R.string.home_definition_plausible,
                R.string.home_example_plausible, R.string.home_prompt_plausible),
            HomeWord(R.string.home_word_meticulous, R.string.home_definition_meticulous,
                R.string.home_example_meticulous, R.string.home_prompt_meticulous),
            HomeWord(R.string.home_word_versatile, R.string.home_definition_versatile,
                R.string.home_example_versatile, R.string.home_prompt_versatile),
            HomeWord(R.string.home_word_coherent, R.string.home_definition_coherent,
                R.string.home_example_coherent, R.string.home_prompt_coherent)
        )

        fun forDay(epochDay: Long): HomeDailyContent {
            val wordIndex = Math.floorMod(epochDay, words.size.toLong()).toInt()
            val answerIndex = (wordIndex + 2) % words.size
            val answer = words[answerIndex]
            val choices = listOf(
                answer,
                words[(answerIndex + 1) % words.size],
                words[(answerIndex + 3) % words.size]
            ).shuffled(kotlin.random.Random(epochDay))
            return HomeDailyContent(words[wordIndex], answer, choices)
        }
    }
}
