package com.danghung.elearning.api.req

import java.io.Serializable

data class ResendVerificationReq(
    val email: String
) : Serializable
