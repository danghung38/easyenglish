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
import retrofit2.http.GET
import retrofit2.http.Query
import com.danghung.elearning.api.res.EvaluationJobRes

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

    @FormUrlEncoded
    @POST("ielts/writing/feedback-by-exam-part")
    fun requestWritingFeedback(
        @Header("Authorization") token: String,
        @Field("userExamPartId") userExamPartId: Long
    ): Call<ApiRes<Map<String, @JvmSuppressWildcards Any?>>>

    @Multipart
    @POST("ielts/speaking/upload-part")
    fun evaluateSpeaking(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("questionId") questionId: RequestBody,
        @Part("userExamPartId") userExamPartId: RequestBody
    ): Call<ApiRes<IELTSSpeakingRes>>

    @FormUrlEncoded
    @POST("ielts/speaking/complete-by-exam-part")
    fun completeSpeaking(@Header("Authorization") token: String,
        @Field("userExamPartId") id: Long): Call<ApiRes<EvaluationJobRes>>

    @GET("ielts/speaking/result-by-exam-part")
    fun getSpeakingResult(@Header("Authorization") token: String,
        @Query("userExamPartId") id: Long): Call<ApiRes<EvaluationJobRes>>

    @FormUrlEncoded
    @POST("ielts/speaking/feedback-by-exam-part")
    fun requestSpeakingFeedback(@Header("Authorization") token: String,
        @Field("userExamPartId") id: Long): Call<ApiRes<EvaluationJobRes>>

    @POST("userexamparts/speaking/submit")
    fun submitSpeaking(
        @Header("Authorization") token: String,
        @Body req: SubmitSpeakingExamReq
    ): Call<ApiRes<Any>>
}
