package com.danghung.elearning.api.req

import java.io.Serializable

data class VerifyOtpReq(
    val otp: String
) : Serializable
