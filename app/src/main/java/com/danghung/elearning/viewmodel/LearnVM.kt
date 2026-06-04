package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserExamApi
import com.danghung.elearning.api.VocabularyApi
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TestHistoryRes
import com.danghung.elearning.api.res.TopicRes
import com.danghung.elearning.api.res.UserExamRes

class LearnVM : BaseViewModel() {
    companion object {
        const val GET_TOPICS = "GET_TOPICS"
        const val GET_HISTORY = "GET_HISTORY"
        const val GET_USER_EXAM = "GET_USER_EXAM"
    }

    fun getTopics() {
        executeApi {
            api(VocabularyApi::class.java).getTopics("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(GET_TOPICS))
        }
    }

    fun getHistory() {
        executeApi {
            api(UserExamApi::class.java).getTestHistory("Bearer ${token ?: ""}", 99)
                .enqueue(initHandleResponse(GET_HISTORY))
        }
    }

    fun getUserExam(userExamId: Long) {
        executeApi {
            api(UserExamApi::class.java).getUserExam("Bearer ${token ?: ""}", userExamId)
                .enqueue(initHandleResponse(GET_USER_EXAM))
        }
    }


    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_TOPICS -> {
                val res = data as ApiRes<List<TopicRes>>
                callBack?.apiSuccess(GET_TOPICS, res.result)
            }

            GET_HISTORY -> {
                val res = data as ApiRes<List<TestHistoryRes>>
                callBack?.apiSuccess(GET_HISTORY, res.result)
            }

            GET_USER_EXAM -> {
                val res = data as ApiRes<UserExamRes>
                callBack?.apiSuccess(GET_USER_EXAM, res.result)
            }
        }
    }
}