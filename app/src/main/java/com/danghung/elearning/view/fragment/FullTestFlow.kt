package com.danghung.elearning.view.fragment

import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.UserExamRes
import com.danghung.elearning.model.StartPracticeData

object FullTestFlow {
    private val orderedSkills = listOf(
        PracticeFragment.LISTENING,
        PracticeFragment.READING,
        PracticeFragment.SPEAKING,
        PracticeFragment.WRITING
    )

    fun buildFirstStartData(exam: ExamRes, userExam: UserExamRes): StartPracticeData? {
        orderedSkills.forEach { skill ->
            buildStartData(exam, userExam, skill)?.let { return it }
        }
        return null
    }

    fun getNextStartData(current: StartPracticeData): StartPracticeData? {
        val currentSkill = current.examPartRes?.skillType ?: return null
        val userExam = current.userExamRes ?: return null
        val currentIndex = orderedSkills.indexOfFirst { it.equals(currentSkill, ignoreCase = true) }
        if (currentIndex == -1) return null

        for (index in currentIndex + 1 until orderedSkills.size) {
            val skill = orderedSkills[index]
            buildStartData(current.exam, userExam, skill)?.let { return it }
        }
        return null
    }

    fun getFragmentTag(skillType: String?): String? {
        return when (skillType?.uppercase()) {
            PracticeFragment.READING -> ReadingFragment.TAG
            PracticeFragment.LISTENING -> ListeningFragment.TAG
            PracticeFragment.SPEAKING -> SpeakingFragment.TAG
            PracticeFragment.WRITING -> WritingFragment.TAG
            else -> null
        }
    }

    private fun buildStartData(
        exam: ExamRes,
        userExam: UserExamRes,
        skillType: String
    ): StartPracticeData? {
        val examPart = exam.examParts.firstOrNull { it.skillType.equals(skillType, ignoreCase = true) }
            ?: return null
        val userExamPart = userExam.parts.firstOrNull { it.skillType.equals(skillType, ignoreCase = true) }
            ?: return null
        return StartPracticeData(
            userExamPartId = userExamPart.id,
            exam = exam,
            examPartRes = examPart,
            userExamRes = userExam
        )
    }
}
