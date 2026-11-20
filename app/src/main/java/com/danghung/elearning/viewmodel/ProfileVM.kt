package com.danghung.elearning.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.UserUpdateReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.UserRes
import com.danghung.elearning.util.UserInputValidator
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileVM : BaseViewModel() {
    companion object {
        const val GET_MY_INFO = "GET_MY_INFO"
        const val UPDATE_USER = "UPDATE_USER"
    }

    private fun avatarPart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val file = File(context.cacheDir, "avatar_temp.$extension")

        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null

        val reqFile = file.asRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData("file", file.name, reqFile)
    }

    fun validateDateOfBirth(date: String): String? {
        return UserInputValidator.dateOfBirthError(date)
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
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }
}
