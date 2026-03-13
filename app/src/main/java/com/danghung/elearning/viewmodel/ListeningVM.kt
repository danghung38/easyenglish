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

class ListeningVM : BaseViewModel() {
    companion object {
        const val GET_QUESTIONS_LISTENING = "GET_QUESTIONS_LISTENING"
        const val SUBMIT_LISTENING = "SUBMIT_LISTENING"
        const val GET_LISTENING_HISTORY = "GET_LISTENING_HISTORY"
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
                .enqueue(initHandleResponse(GET_QUESTIONS_LISTENING))
        }
    }

    fun submit(userExamPartId: Long) {
        val req = SubmitRLPartReq(userExamPartId, answerMap.values.toList())
        executeApi {
            api(UserExamPartApi::class.java).submitRLPart("Bearer ${token ?: ""}", req)
                .enqueue(initHandleResponse(SUBMIT_LISTENING))
        }
    }

    fun getListeningHistory(userExamPartId: Long) {
        executeApi {
            api(UserAnswerApi::class.java).getUserAnswers("Bearer ${token ?: ""}", userExamPartId)
                .enqueue(initHandleResponse(GET_LISTENING_HISTORY))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_QUESTIONS_LISTENING -> {
                val res = data as ApiRes<PageRes<List<QuestionRes>>>
                callBack?.apiSuccess(GET_QUESTIONS_LISTENING, res.result?.items)
            }

            SUBMIT_LISTENING -> {
                val res = data as ApiRes<SubmitRLPartRes>
                callBack?.apiSuccess(SUBMIT_LISTENING, res.result)
            }

            GET_LISTENING_HISTORY -> {
                val res = data as ApiRes<List<UserAnswerRes>>
                callBack?.apiSuccess(GET_LISTENING_HISTORY, res.result)
            }
        }
    }
}