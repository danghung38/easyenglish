package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.QuestionApi
import com.danghung.elearning.api.UserAnswerApi
import com.danghung.elearning.api.UserExamPartApi
import com.danghung.elearning.api.req.SubmitSpeakingExamReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.UserAnswerRes
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SpeakingVM : BaseViewModel() {

    companion object {
        const val GET_QUESTIONS_SPEAKING = "GET_QUESTIONS_SPEAKING"
        const val EVALUATE_SPEAKING = "EVALUATE_SPEAKING"
        const val SUBMIT_SPEAKING = "SUBMIT_SPEAKING"
        const val GET_SPEAKING_HISTORY = "GET_SPEAKING_HISTORY"
    }

    override fun onCleared() {
        stopCountdown()
        super.onCleared()
    }

    fun getQuestions(examPartId: Long) {
        executeApi {
            api(QuestionApi::class.java).getQuestions(examPartId)
                .enqueue(initHandleResponse(GET_QUESTIONS_SPEAKING))
        }
    }

    fun evaluateSpeaking(
        file: File, questionId: Long, userExamPartId: Long
    ) {

        val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())

        val multipartFile = MultipartBody.Part.createFormData(
            "file", file.name, requestFile
        )

        val questionBody = questionId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val userExamPartBody =
            userExamPartId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        executeApi {
            api(UserExamPartApi::class.java).evaluateSpeaking(
                "Bearer ${token ?: ""}", multipartFile, questionBody, userExamPartBody
            ).enqueue(initHandleResponse(EVALUATE_SPEAKING))
        }
    }

    fun submitSpeaking(req: SubmitSpeakingExamReq) {
        executeApi {
            api(UserExamPartApi::class.java).submitSpeaking("Bearer ${token ?: ""}", req)
                .enqueue(initHandleResponse(SUBMIT_SPEAKING))
        }
    }

    fun getSpeakingHistory(userExamPartId: Long) {
        executeApi {
            api(UserAnswerApi::class.java).getUserAnswers("Bearer ${token ?: ""}", userExamPartId)
                .enqueue(initHandleResponse(GET_SPEAKING_HISTORY))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_QUESTIONS_SPEAKING -> {
                val res = data as ApiRes<PageRes<List<QuestionRes>>>
                callBack?.apiSuccess(GET_QUESTIONS_SPEAKING, res.result?.items)
            }

            EVALUATE_SPEAKING -> {
                val res = data as ApiRes<IELTSSpeakingRes>
                callBack?.apiSuccess(EVALUATE_SPEAKING, res.result)
            }

            SUBMIT_SPEAKING -> {
                callBack?.apiSuccess(SUBMIT_SPEAKING, null)
            }

            GET_SPEAKING_HISTORY -> {
                val res = data as ApiRes<List<UserAnswerRes>>
                callBack?.apiSuccess(GET_SPEAKING_HISTORY, res.result)
            }
        }
    }
}