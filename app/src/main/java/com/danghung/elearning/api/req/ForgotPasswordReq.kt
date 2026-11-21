package com.danghung.elearning.api.req

import java.io.Serializable

data class ForgotPasswordReq(
    val email: String
) : Serializable
