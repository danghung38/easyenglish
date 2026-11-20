package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TopicRes
import com.danghung.elearning.api.res.VocabularyRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VocabularyApi {

    @GET("vocabulary/topics")
    fun getTopics(@Header("Authorization") token: String
    ): Call<ApiRes<List<TopicRes>>>

    @GET("vocabulary/topics/{topicName}/words")
    fun getVocabulariesByTopic(
        @Header("Authorization") token: String,
        @Path("topicName") topicName: String
    ): Call<ApiRes<List<VocabularyRes>>>
}