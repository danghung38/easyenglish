package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.UserApi
import com.danghung.elearning.api.req.UserCreationReq

class RegisterVM : BaseViewModel() {
    companion object {
        const val CREATE_USER = "CREATE_USER"
    }

    fun register(req: UserCreationReq) {
        executeApi {
            api(UserApi::class.java).createUser(req).enqueue(initHandleResponse(CREATE_USER))
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
        }
    }

}