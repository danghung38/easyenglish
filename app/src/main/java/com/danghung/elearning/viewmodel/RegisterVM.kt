package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.UserCreationReq
import com.danghung.elearning.api.req.ResendVerificationReq
import com.danghung.elearning.api.req.VerifyOtpReq
import com.danghung.elearning.util.UserInputValidator

class RegisterVM : BaseViewModel() {
    companion object {
        const val CREATE_USER = "CREATE_USER"
        const val VERIFY_REGISTER = "VERIFY_REGISTER"
        const val RESEND_VERIFICATION = "RESEND_VERIFICATION"
    }

    override fun requiresAuthenticatedSession(key: String): Boolean =
        key != CREATE_USER && key != VERIFY_REGISTER && key != RESEND_VERIFICATION

    fun register(req: UserCreationReq) {
        executeApi {
            api(UserApi::class.java).createUser(req).enqueue(initHandleResponse(CREATE_USER))
        }
    }

    fun verifyOtp(otp: String) {
        executeApi {
            api(UserApi::class.java)
                .verifyRegister(VerifyOtpReq(otp))
                .enqueue(initHandleResponse(VERIFY_REGISTER))
        }
    }

    fun resendVerification(email: String) {
        executeApi {
            api(UserApi::class.java)
                .resendVerification(ResendVerificationReq(email))
                .enqueue(initHandleResponse(RESEND_VERIFICATION))
        }
    }

    fun verifyRegister(
        fullName: String,
        username: String,
        email: String,
        phoneNumber: String,
        gender: String,
        dob: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (fullName.isBlank()) return "Full name is required"
        if (username.isBlank()) return "Username is required"
        if (username.length < 6) return "Username must be at least 6 characters"
        if (email.isBlank()) return "Email is required"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) return "Invalid email format"
        if (phoneNumber.isBlank()) return "Phone number is required"
        if (phoneNumber.length < 9) return "Invalid phone number"
        if (gender.isBlank() || gender == "Select gender") return "Please select gender"
        if (dob.isBlank()) return "Date of birth is required"
        UserInputValidator.dateOfBirthError(dob)?.let { return it }
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (confirmPassword.isBlank()) return "Confirm password is required"
        if (password != confirmPassword) return "Password and Confirm password do not match"
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            CREATE_USER -> {
                callBack?.apiSuccess(CREATE_USER, null)
            }
            VERIFY_REGISTER, RESEND_VERIFICATION -> {
                callBack?.apiSuccess(key ?: "", null)
            }
            else -> {
                super.handleSuccess(key, data)
            }
        }
    }

}
