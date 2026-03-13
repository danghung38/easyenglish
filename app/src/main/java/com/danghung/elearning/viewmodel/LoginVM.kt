package com.danghung.elearning.viewmodel

import com.danghung.elearning.CommonUtils
import com.danghung.elearning.api.AuthApi
import com.danghung.elearning.api.req.AuthReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.AuthRes

class LoginVM : BaseViewModel() {
    companion object {
        const val LOGIN = "AUTH_LOGIN"
        const val KEY_TOKEN = "KEY_TOKEN"
    }

    fun login(username: String, password: String) {
        val req = AuthReq(username, password)

        executeApi {
            api(AuthApi::class.java)
                .login(req)
                .enqueue(initHandleResponse(LOGIN))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            LOGIN -> {
                val res = data as ApiRes<AuthRes>
                val token = res.result?.token
                //val refreshToken = res.result?.refreshToken
                CommonUtils.getInstance().savePref(KEY_TOKEN, token ?: "")
                callBack?.apiSuccess(LOGIN, null)
            }
        }
    }


}