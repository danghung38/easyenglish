package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.VocabularyApi
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.VocabularyRes

class VocabularyVM : BaseViewModel() {
    companion object {
        const val GET_VOCABULARY = "GET_VOCABULARY"
    }


    fun getVocabularies(topicName: String) {
       executeApi {
           api(VocabularyApi::class.java).getVocabulariesByTopic("Bearer ${token ?: ""}", topicName)
               .enqueue(initHandleResponse(GET_VOCABULARY))
       }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_VOCABULARY -> {
                val res = data as ApiRes<List<VocabularyRes>>
                callBack?.apiSuccess(GET_VOCABULARY, res.result)
            }
        }
    }
}