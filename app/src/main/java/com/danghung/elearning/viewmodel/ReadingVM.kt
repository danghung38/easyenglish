package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.QuestionApi
import com.danghung.elearning.api.UserAnswerApi
import com.danghung.elearning.api.UserExamPartApi
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.req.SubmitRLPartReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.SubmitRLPartRes
import com.danghung.elearning.api.res.UserAnswerRes

class ReadingVM : BaseViewModel() {
    companion object {
        const val GET_QUESTIONS_READING = "GET_QUESTIONS_READING"
        const val SUBMIT_READING = "SUBMIT_READING"
        const val GET_READING_HISTORY = "GET_READING_HISTORY"
    }

    private val answerMap = mutableMapOf<Long, AnswerRLPartReq>()

    fun getAnsweredCount(): Int = answerMap.size

    override fun onCleared() {
        stopCountdown()
        super.onCleared()
    }

    fun validateBeforeSubmit(): String? {
        return if (answerMap.isEmpty()) {
            "You have not answered any questions!"
        } else null
    }

    fun addOrUpdateAnswer(answer: AnswerRLPartReq) {
        answerMap[answer.questionId] = answer
    }

    fun getQuestions(examPartId: Long) {
        executeApi {
            api(QuestionApi::class.java).getQuestions(examPartId)
                .enqueue(initHandleResponse(GET_QUESTIONS_READING))
        }
    }

    fun submit(userExamPartId: Long) {
        val req = SubmitRLPartReq(userExamPartId, answerMap.values.toList())
        executeApi {
            api(UserExamPartApi::class.java).submitRLPart("Bearer ${token ?: ""}", req)
                .enqueue(initHandleResponse(SUBMIT_READING))
        }
    }

    fun getReadingHistory(userExamPartId: Long) {
        executeApi {
            api(UserAnswerApi::class.java).getUserAnswers("Bearer ${token ?: ""}", userExamPartId)
                .enqueue(initHandleResponse(GET_READING_HISTORY))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_QUESTIONS_READING -> {
                val res = data as ApiRes<PageRes<List<QuestionRes>>>
                callBack?.apiSuccess(GET_QUESTIONS_READING, res.result?.items)
            }

            SUBMIT_READING -> {
                val res = data as ApiRes<SubmitRLPartRes>
                callBack?.apiSuccess(SUBMIT_READING, res.result)
            }

            GET_READING_HISTORY -> {
                val res = data as ApiRes<List<UserAnswerRes>>
                callBack?.apiSuccess(GET_READING_HISTORY, res.result)
            }
        }
    }
}