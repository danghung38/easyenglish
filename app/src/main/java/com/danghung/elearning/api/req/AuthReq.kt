package com.danghung.elearning.api.req

import java.io.Serializable

data class AuthReq(
    val username: String,
    val password: String
): Serializable