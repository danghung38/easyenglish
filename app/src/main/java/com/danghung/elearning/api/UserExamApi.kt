package com.danghung.elearning.api

import com.danghung.elearning.api.req.UserExamReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TestHistoryRes
import com.danghung.elearning.api.res.UserExamRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface UserExamApi {
    @GET("userexams/test-history")
    fun getTestHistory(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 99
    ): Call<ApiRes<List<TestHistoryRes>>>

    @POST("userexams")
    fun createExam(
        @Header("Authorization") token: String,
        @Body req: UserExamReq
    ): Call<ApiRes<UserExamRes>>
}