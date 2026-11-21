package com.danghung.elearning.api.req

import java.io.Serializable

data class TwoFactorCodeReq(
    val code: String
) : Serializable
