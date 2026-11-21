package com.danghung.elearning.viewmodel

import com.danghung.elearning.CommonUtils
import com.danghung.elearning.api.AuthApi
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.AuthReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.AuthRes

class LoginVM : BaseViewModel() {
    companion object {
        const val LOGIN = "AUTH_LOGIN"
        const val GOOGLE_LOGIN = "AUTH_GOOGLE_LOGIN"
        const val LOGIN_2FA = "AUTH_LOGIN_2FA"
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_REFRESH_TOKEN = "KEY_REFRESH_TOKEN"
        const val VERIFY_TOKEN = "VERIFY_TOKEN"
    }

    fun login(username: String, password: String) {
        val req = AuthReq(username, password)
        executeApi {
            api(AuthApi::class.java).login(req).enqueue(initHandleResponse(LOGIN))
        }
    }

    fun loginWithGoogle(idToken: String) {
        executeApi {
            api(AuthApi::class.java)
                .loginWithGoogle(com.danghung.elearning.api.req.GoogleLoginReq(idToken))
                .enqueue(initHandleResponse(GOOGLE_LOGIN))
        }
    }

    fun loginWithTwoFactor(tempToken: String, code: String) {
        executeApi {
            api(AuthApi::class.java)
                .loginWithTwoFactor(
                    com.danghung.elearning.api.req.TwoFactorLoginReq(tempToken, code)
                )
                .enqueue(initHandleResponse(LOGIN_2FA))
        }
    }

    fun verifyToken() {
        executeApi {
            api(UserApi::class.java)
                .getMyInfo("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(VERIFY_TOKEN))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            LOGIN, GOOGLE_LOGIN, LOGIN_2FA -> {
                val res = data as ApiRes<AuthRes>
                val auth = res.result
                if (auth?.requires2fa == true) {
                    if (auth.tempToken.isNullOrBlank()) {
                        callBack?.apiError(key, 401, null)
                    } else {
                        callBack?.apiSuccess(key, auth)
                    }
                    return
                }
                val accessToken = auth?.accessToken
                val refreshToken = auth?.refreshToken
                if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                    CommonUtils.getInstance().clearPref(KEY_TOKEN)
                    CommonUtils.getInstance().clearPref(KEY_REFRESH_TOKEN)
                    callBack?.apiError(key, 401, null)
                    return
                }
                CommonUtils.getInstance().savePref(KEY_TOKEN, accessToken)
                CommonUtils.getInstance().savePref(KEY_REFRESH_TOKEN, refreshToken)
                callBack?.apiSuccess(key, null)
            }
            VERIFY_TOKEN -> callBack?.apiSuccess(VERIFY_TOKEN, null)
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }
}
