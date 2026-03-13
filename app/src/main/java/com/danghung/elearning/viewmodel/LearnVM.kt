package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserExamApi
import com.danghung.elearning.api.VocabularyApi
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TestHistoryRes
import com.danghung.elearning.api.res.TopicRes

class LearnVM : BaseViewModel() {
    companion object {
        const val GET_TOPICS = "GET_TOPICS"
        const val GET_HISTORY = "GET_HISTORY"
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
        }
    }
}