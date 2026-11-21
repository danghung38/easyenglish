package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.ForgotPasswordReq
import com.danghung.elearning.api.req.ResetPasswordReq

class RecoveryVM : BaseViewModel() {
    companion object {
        const val FORGOT_PASSWORD = "FORGOT_PASSWORD"
        const val RESET_PASSWORD = "RESET_PASSWORD"
    }

    fun forgotPassword(email: String) {
        executeApi {
            api(UserApi::class.java)
                .forgotPassword(ForgotPasswordReq(email))
                .enqueue(initHandleResponse(FORGOT_PASSWORD))
        }
    }

    fun resetPassword(resetCode: String, newPassword: String) {
        executeApi {
            api(UserApi::class.java)
                .resetPassword(ResetPasswordReq(resetCode, newPassword))
                .enqueue(initHandleResponse(RESET_PASSWORD))
        }
    }

    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            FORGOT_PASSWORD, RESET_PASSWORD -> callBack?.apiSuccess(key ?: "", null)
            else -> super.handleSuccess(key, data)
        }
    }
}
