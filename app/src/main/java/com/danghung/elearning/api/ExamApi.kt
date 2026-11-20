package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.PageRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ExamApi {
    @GET("exams/list")
    fun getExamList(
        @Query("pageNo") pageNo: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String? = null
    ): Call<ApiRes<PageRes<List<ExamRes>>>>

}