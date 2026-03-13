package com.danghung.elearning.api.req

import java.io.Serializable

data class LogoutReq(
    val token: String
) : Serializable