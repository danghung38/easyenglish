package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.QuestionApi
import com.danghung.elearning.api.UserAnswerApi
import com.danghung.elearning.api.UserExamPartApi
import com.danghung.elearning.api.req.SubmitWritingExamReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.IELTSWritingRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.api.res.WritingResultRes

class WritingVM : BaseViewModel() {
    companion object {
        const val GET_QUESTIONS_WRITING = "GET_QUESTIONS_WRITING"
        const val SUBMIT_WRITING = "SUBMIT_WRITING"
        const val EVALUATE_WRITING = "EVALUATE_WRITING"
        const val GET_WRITING_HISTORY = "GET_WRITING_HISTORY"
    }

    override fun onCleared() {
        stopCountdown()
        super.onCleared()
    }

    fun getQuestions(examPartId: Long) {
        executeApi {
            api(QuestionApi::class.java).getQuestions(examPartId)
                .enqueue(initHandleResponse(GET_QUESTIONS_WRITING))
        }
    }

    fun evaluateWriting(
        essay: String, questionId: Long, wordCount: Int, duration: Int
    ) {
        executeApi {
            api(UserExamPartApi::class.java).evaluateWriting(
                "Bearer ${token ?: ""}", essay, questionId, wordCount, duration
            ).enqueue(initHandleResponse(EVALUATE_WRITING))
        }
    }

    fun submitWriting(req: SubmitWritingExamReq) {
        executeApi {
            api(UserExamPartApi::class.java).submitWriting("Bearer ${token ?: ""}", req)
                .enqueue(initHandleResponse(SUBMIT_WRITING))
        }
    }

    fun getWritingHistory(userExamPartId: Long) {
        executeApi {
            api(UserAnswerApi::class.java).getUserAnswers("Bearer ${token ?: ""}", userExamPartId)
                .enqueue(initHandleResponse(GET_WRITING_HISTORY))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_QUESTIONS_WRITING -> {
                val res = data as ApiRes<PageRes<List<QuestionRes>>>
                callBack?.apiSuccess(GET_QUESTIONS_WRITING, res.result?.items)
            }

            EVALUATE_WRITING -> {
                val res = data as ApiRes<IELTSWritingRes>
                callBack?.apiSuccess(EVALUATE_WRITING, res.result)
            }

            SUBMIT_WRITING -> {
                val res = data as ApiRes<WritingResultRes>
                callBack?.apiSuccess(SUBMIT_WRITING, res.result)
            }

            GET_WRITING_HISTORY -> {
                val res = data as ApiRes<List<UserAnswerRes>>
                callBack?.apiSuccess(GET_WRITING_HISTORY, res.result)
            }
        }
    }
}