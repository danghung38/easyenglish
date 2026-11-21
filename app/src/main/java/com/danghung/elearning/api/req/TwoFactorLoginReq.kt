package com.danghung.elearning.api.req

import java.io.Serializable

data class TwoFactorLoginReq(
    val tempToken: String,
    val code: String
) : Serializable
