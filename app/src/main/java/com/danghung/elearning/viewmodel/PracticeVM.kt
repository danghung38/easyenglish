package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserExamApi
import com.danghung.elearning.api.req.UserExamReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.ExamPartRes
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.UserExamRes

class PracticeVM : BaseViewModel() {
    companion object {
        const val START_SINGLE = "START_SINGLE"
        const val START_FULL = "START_FULL"
    }

    fun getExamPartBySkill(exam: ExamRes, skill: String): ExamPartRes? {
        return exam.examParts.firstOrNull {
            it.skillType.equals(skill, ignoreCase = true)
        }
    }

    fun startFullTest(userExam: UserExamReq) {
        executeApi {
            api(UserExamApi::class.java).createExam("Bearer $token", userExam)
                .enqueue(initHandleResponse(START_FULL))
        }
    }

    fun startSingleSkill(userExam: UserExamReq) {
        executeApi {
            api(UserExamApi::class.java).createExam("Bearer $token", userExam)
                .enqueue(initHandleResponse(START_SINGLE))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            START_SINGLE -> {
                val res = data as ApiRes<UserExamRes>
                val userExamPartId = res.result?.parts?.firstOrNull()?.id

                callBack?.apiSuccess(START_SINGLE, userExamPartId)
            }

            START_FULL -> {
                val res = data as ApiRes<UserExamRes>
                callBack?.apiSuccess(START_FULL, res.result)
            }
        }
    }


}