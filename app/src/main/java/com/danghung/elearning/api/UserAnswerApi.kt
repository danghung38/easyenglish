package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.UserAnswerRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserAnswerApi {
    @GET("useranswers/answers/{userExamPartId}")
    fun getUserAnswers(
        @Header("Authorization") token: String,
        @Path("userExamPartId") userExamPartId: Long
    ): Call<ApiRes<List<UserAnswerRes>>>
}