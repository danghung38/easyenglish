package com.danghung.elearning.view.adapter

import com.danghung.elearning.api.res.QuestionRes
import java.util.Locale

/** Keeps section headers and answerable questions in a deterministic display order. */
internal object QuestionListPresentation {
    private val sectionNumberPattern = Regex("\\d+")

    fun order(items: List<QuestionRes>): List<QuestionRes> = items.sortedWith(
        compareBy<QuestionRes> { sectionNumber(it.section) ?: Int.MAX_VALUE }
            .thenBy { it.section.orEmpty().lowercase(Locale.ROOT) }
            .thenBy { if (it.isSection) 0 else 1 }
            .thenBy { it.id }
    )

    fun answerableCount(items: List<QuestionRes>): Int = items.count { !it.isSection }

    fun questionNumberAt(items: List<QuestionRes>, position: Int): Int =
        items.take(position + 1).count { !it.isSection }

    private fun sectionNumber(section: String?): Int? =
        sectionNumberPattern.find(section.orEmpty())?.value?.toIntOrNull()
}
