package com.danghung.elearning.evaluation

import com.danghung.elearning.api.res.QuestionRes

/** A recording/import covers all prompts of the current speaking part. */
object SpeakingParts {
    fun group(questions: List<QuestionRes>): List<List<QuestionRes>> =
        listOf("part1", "part2", "part3").mapNotNull { part ->
            questions.filter { it.section.equals(part, ignoreCase = true) }
                .sortedBy { it.id }.takeIf { it.isNotEmpty() }
        }
}
