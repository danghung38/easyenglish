package com.danghung.elearning.api.req

import java.io.Serializable

data class ResetPasswordReq(
    val resetCode: String,
    val newPassword: String
) : Serializable
