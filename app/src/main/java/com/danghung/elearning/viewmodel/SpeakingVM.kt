package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.QuestionApi
import com.danghung.elearning.api.UserAnswerApi
import com.danghung.elearning.api.UserExamPartApi
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
import com.danghung.elearning.api.res.EvaluationJobRes

class SpeakingVM : BaseViewModel() {

    companion object {
        const val GET_QUESTIONS_SPEAKING = "GET_QUESTIONS_SPEAKING"
        const val EVALUATE_SPEAKING = "EVALUATE_SPEAKING"
        const val GET_SPEAKING_HISTORY = "GET_SPEAKING_HISTORY"
        const val SPEAKING_RESULT = "SPEAKING_RESULT"
        const val SPEAKING_FEEDBACK = "SPEAKING_FEEDBACK"
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
        file: File,
        questionId: Long,
        userExamPartId: Long,
        mimeType: String = "audio/mp4"
    ) {
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

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

    fun completeSpeaking(id: Long) = executeApi {
        api(UserExamPartApi::class.java).completeSpeaking("Bearer ${token ?: ""}", id)
            .enqueue(initHandleResponse(SPEAKING_RESULT))
    }

    fun getSpeakingResult(id: Long) = executeApi {
        api(UserExamPartApi::class.java).getSpeakingResult("Bearer ${token ?: ""}", id)
            .enqueue(initHandleResponse(SPEAKING_RESULT))
    }

    fun requestSpeakingFeedback(id: Long) = executeApi {
        api(UserExamPartApi::class.java).requestSpeakingFeedback("Bearer ${token ?: ""}", id)
            .enqueue(initHandleResponse(SPEAKING_FEEDBACK))
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

            GET_SPEAKING_HISTORY -> {
                val res = data as ApiRes<List<UserAnswerRes>>
                callBack?.apiSuccess(GET_SPEAKING_HISTORY, res.result)
            }
            SPEAKING_RESULT, SPEAKING_FEEDBACK -> {
                val res = data as ApiRes<EvaluationJobRes>
                callBack?.apiSuccess(key, res.result)
            }
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }
}
