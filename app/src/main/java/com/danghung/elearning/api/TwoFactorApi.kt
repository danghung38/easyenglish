package com.danghung.elearning.api

import com.danghung.elearning.api.req.TwoFactorCodeReq
import com.danghung.elearning.api.req.TwoFactorDisableReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TwoFactorSetupRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TwoFactorApi {
    @POST("2fa/setup")
    fun setup(@Header("Authorization") token: String): Call<ApiRes<TwoFactorSetupRes>>

    @POST("2fa/verify-enable")
    fun verifyEnable(
        @Header("Authorization") token: String,
        @Body request: TwoFactorCodeReq
    ): Call<ApiRes<Any>>

    @POST("2fa/disable")
    fun disable(
        @Header("Authorization") token: String,
        @Body request: TwoFactorDisableReq
    ): Call<ApiRes<Any>>
}
