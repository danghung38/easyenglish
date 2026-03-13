package com.danghung.elearning.viewmodel

import android.content.Context
import android.net.Uri
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.UserUpdateReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.UserRes
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileVM : BaseViewModel() {
    companion object {
        const val GET_MY_INFO = "GET_MY_INFO"
        const val UPDATE_USER = "UPDATE_USER"
    }

    private fun avatarPart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "avatar_temp.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        val reqFile = file.asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData("file", file.name, reqFile)
    }

    fun updateUser(context: Context, req: UserUpdateReq, avatarUri: Uri?) {
        val userBody = Gson().toJson(req).toRequestBody("application/json".toMediaType())
        val avatarPart = avatarPart(context, avatarUri)
        executeApi {
            api(UserApi::class.java).updateUser("Bearer $token", userBody, avatarPart)
                .enqueue(initHandleResponse(UPDATE_USER))
        }
    }

    fun getMyInfo() {
        executeApi {
            api(UserApi::class.java).getMyInfo("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(GET_MY_INFO))
        }
    }

    fun isValidDate(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_MY_INFO -> {
                val res = data as ApiRes<UserRes>
                callBack?.apiSuccess(GET_MY_INFO, res.result)
            }

            UPDATE_USER -> {
                callBack?.apiSuccess(UPDATE_USER, null)
            }
        }
    }
}