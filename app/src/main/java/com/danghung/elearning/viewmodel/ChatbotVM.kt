package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.ChatbotApi
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.ChatMessageReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.UserRes
import okhttp3.ResponseBody

class ChatbotVM : BaseViewModel() {
    companion object {
        const val SEND_CHAT = "SEND_CHAT"
        const val GET_MY_INFO = "GET_MY_INFO"
    }

    var avatarUrl: String? = null  // lưu lại để dùng nhiều lần

    fun getMyInfo() {
        executeApi {
            api(UserApi::class.java).getMyInfo("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(GET_MY_INFO))
        }
    }

    fun sendChat(message: String) {
        val request = ChatMessageReq(message)
        executeApi {
            api(ChatbotApi::class.java).sendChatMessage("Bearer $token", request)
                .enqueue(initHandleResponse(SEND_CHAT))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_MY_INFO -> {
                val res = data as ApiRes<UserRes>
                avatarUrl = res.result?.avatar
                callBack?.apiSuccess(GET_MY_INFO, res.result)
            }
            SEND_CHAT -> {
                val responseBody = data as? ResponseBody
                val botReply = responseBody?.string() ?: "Hệ thống bận, vui lòng thử lại sau."
                callBack?.apiSuccess(SEND_CHAT, botReply)
            }
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }
}