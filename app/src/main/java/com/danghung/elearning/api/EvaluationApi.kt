package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.EvaluationJobRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EvaluationApi {
    @POST("evaluations/{skill}")
    fun create(
        @Header("Authorization") token: String,
        @Path("skill") skill: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Call<ApiRes<EvaluationJobRes>>

    @GET("evaluations/{jobId}")
    fun get(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Call<ApiRes<EvaluationJobRes>>

    @POST("evaluations/{jobId}/cancel")
    fun cancel(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Call<ApiRes<EvaluationJobRes>>

    @POST("evaluations/{jobId}/feedback")
    fun requestFeedback(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Call<ApiRes<EvaluationJobRes>>
}
