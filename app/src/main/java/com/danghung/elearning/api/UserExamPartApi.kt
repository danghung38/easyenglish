package com.danghung.elearning.api

import com.danghung.elearning.api.req.SubmitRLPartReq
import com.danghung.elearning.api.req.SubmitSpeakingExamReq
import com.danghung.elearning.api.req.SubmitWritingExamReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.IELTSWritingRes
import com.danghung.elearning.api.res.SubmitRLPartRes
import com.danghung.elearning.api.res.WritingResultRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserExamPartApi {
    @POST("userexamparts")
    fun submitRLPart(
        @Header("Authorization") token: String,
        @Body req: SubmitRLPartReq
    ): Call<ApiRes<SubmitRLPartRes>>

    @FormUrlEncoded
    @POST("ielts/writing/evaluate-by-question")
    fun evaluateWriting(
        @Header("Authorization") token: String,
        @Field("essayText") essayText: String,
        @Field("questionId") questionId: Long,
        @Field("wordCount") wordCount: Int?,
        @Field("duration") duration: Int?
    ): Call<ApiRes<IELTSWritingRes>>

    @POST("userexamparts/writing/submit")
    fun submitWriting(
        @Header("Authorization") token: String,
        @Body req: SubmitWritingExamReq
    ): Call<ApiRes<WritingResultRes>>

    @Multipart
    @POST("ielts/speaking/evaluate-by-question")
    fun evaluateSpeaking(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("questionId") questionId: RequestBody,
        @Part("userExamPartId") userExamPartId: RequestBody
    ): Call<ApiRes<IELTSSpeakingRes>>

    @POST("userexamparts/speaking/submit")
    fun submitSpeaking(
        @Header("Authorization") token: String,
        @Body req: SubmitSpeakingExamReq
    ): Call<ApiRes<Any>>
}