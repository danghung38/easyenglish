package com.danghung.elearning.api.req

import java.io.Serializable

data class TwoFactorDisableReq(
    val password: String,
    val code: String
) : Serializable
