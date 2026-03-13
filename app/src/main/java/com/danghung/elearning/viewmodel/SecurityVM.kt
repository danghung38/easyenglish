package com.danghung.elearning.viewmodel

import com.danghung.elearning.CommonUtils
import com.danghung.elearning.api.AuthApi
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.ChangePasswordReq
import com.danghung.elearning.api.req.LogoutReq

class SecurityVM: BaseViewModel() {
    companion object {
        const val LOGOUT = "LOGOUT"
        const val CHANGE_PASSWORD = "CHANGE_PASSWORD"
    }

    fun logout() {
        executeApi {
            api(AuthApi::class.java)
                .logout(LogoutReq(token ?: ""))
                .enqueue(initHandleResponse(LOGOUT))
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        val req = ChangePasswordReq(oldPass, newPass)

        executeApi {
            api(UserApi::class.java)
                .changePassword("Bearer ${token ?: ""}", req)
                .enqueue(initHandleResponse(CHANGE_PASSWORD))
        }
    }

    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            LOGOUT -> {
                CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
                callBack?.apiSuccess(LOGOUT, null)
            }
            CHANGE_PASSWORD -> {
                callBack?.apiSuccess(CHANGE_PASSWORD, null)
            }
        }
    }
}