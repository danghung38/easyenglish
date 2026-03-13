package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.api.res.QuestionRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuestionApi {
    @GET("questions/list")
    fun getQuestions(
        @Query("examPartId") examPartId: Long,
        @Query("pageNo") pageNo: Int = 0,
        @Query("pageSize") pageSize: Int = 100
    ): Call<ApiRes<PageRes<List<QuestionRes>>>>
}