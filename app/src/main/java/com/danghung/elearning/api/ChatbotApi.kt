package com.danghung.elearning.api

import com.danghung.elearning.api.req.ChatMessageReq
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// API interface
interface ChatbotApi {
    @POST("assistant/chat")
    fun sendChatMessage(
        @Header("Authorization") token: String,
        @Body request: ChatMessageReq
    ): Call<ResponseBody>  // ← dùng ResponseBody
}