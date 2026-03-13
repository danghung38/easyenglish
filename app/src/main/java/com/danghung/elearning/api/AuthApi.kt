package com.danghung.elearning.api

import com.danghung.elearning.api.req.AuthReq
import com.danghung.elearning.api.req.LogoutReq
import com.danghung.elearning.api.req.RefreshReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.AuthRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    fun login(@Body request: AuthReq): Call<ApiRes<AuthRes>>


    @POST("auth/logout")
    fun logout(@Body request: LogoutReq): Call<ApiRes<Void>>

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshReq): Call<ApiRes<AuthRes>>
}