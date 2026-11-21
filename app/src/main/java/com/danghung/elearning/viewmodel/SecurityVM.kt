package com.danghung.elearning.viewmodel

import com.danghung.elearning.CommonUtils
import com.danghung.elearning.api.AuthApi
import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.TwoFactorApi
import com.danghung.elearning.api.req.ChangePasswordReq
import com.danghung.elearning.api.req.LogoutReq
import com.danghung.elearning.api.req.TwoFactorCodeReq
import com.danghung.elearning.api.req.TwoFactorDisableReq
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.TwoFactorSetupRes
import com.danghung.elearning.api.res.UserRes
import com.danghung.elearning.view.fragment.SecurityFragment.Companion.KEY_LOGIN_FINGER

class SecurityVM: BaseViewModel() {
    companion object {
        const val LOGOUT = "LOGOUT"
        const val CHANGE_PASSWORD = "CHANGE_PASSWORD"
        const val GET_MY_INFO = "SECURITY_GET_MY_INFO"
        const val SETUP_2FA = "SETUP_2FA"
        const val VERIFY_ENABLE_2FA = "VERIFY_ENABLE_2FA"
        const val DISABLE_2FA = "DISABLE_2FA"
    }

    fun getMyInfo() {
        executeApi {
            api(UserApi::class.java)
                .getMyInfo("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(GET_MY_INFO))
        }
    }

    fun setupTwoFactor() {
        executeApi {
            api(TwoFactorApi::class.java)
                .setup("Bearer ${token ?: ""}")
                .enqueue(initHandleResponse(SETUP_2FA))
        }
    }

    fun verifyEnableTwoFactor(code: String) {
        executeApi {
            api(TwoFactorApi::class.java)
                .verifyEnable("Bearer ${token ?: ""}", TwoFactorCodeReq(code))
                .enqueue(initHandleResponse(VERIFY_ENABLE_2FA))
        }
    }

    fun disableTwoFactor(password: String, code: String) {
        executeApi {
            api(TwoFactorApi::class.java)
                .disable("Bearer ${token ?: ""}", TwoFactorDisableReq(password, code))
                .enqueue(initHandleResponse(DISABLE_2FA))
        }
    }

    fun logout() {
        executeApi {
            api(AuthApi::class.java)
                .logout(
                    LogoutReq(
                        accessToken = token ?: "",
                        refreshToken = CommonUtils.getInstance()
                            .getPref(LoginVM.KEY_REFRESH_TOKEN) ?: ""
                    )
                )
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
            GET_MY_INFO -> {
                val res = data as ApiRes<UserRes>
                callBack?.apiSuccess(GET_MY_INFO, res.result)
            }
            SETUP_2FA -> {
                val res = data as ApiRes<TwoFactorSetupRes>
                callBack?.apiSuccess(SETUP_2FA, res.result)
            }
            VERIFY_ENABLE_2FA, DISABLE_2FA -> {
                callBack?.apiSuccess(key, null)
            }
            LOGOUT -> {
                CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
                CommonUtils.getInstance().clearPref(LoginVM.KEY_REFRESH_TOKEN)
                CommonUtils.getInstance().clearPref(KEY_LOGIN_FINGER)
                callBack?.apiSuccess(LOGOUT, null)
            }
            CHANGE_PASSWORD -> {
                callBack?.apiSuccess(CHANGE_PASSWORD, null)
            }
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }
}
