package com.danghung.elearning.api

import com.danghung.elearning.api.req.ChangePasswordReq
import com.danghung.elearning.api.req.UserCreationReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.UserRes
import com.danghung.elearning.api.res.UserUpdateRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApi {
    @POST("users")
    fun createUser(@Body request: UserCreationReq): Call<ApiRes<UserRes>>

    @GET("users/myInfo")
    fun getMyInfo(@Header("Authorization") token: String): Call<ApiRes<UserRes>>

    @POST("users/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordReq
    ): Call<ApiRes<Any>>

    @Multipart
    @PUT("users")
    fun updateUser(
        @Header("Authorization") token: String,
        @Part("user") user: RequestBody,
        @Part file: MultipartBody.Part?
    ): Call<ApiRes<UserUpdateRes>>
}