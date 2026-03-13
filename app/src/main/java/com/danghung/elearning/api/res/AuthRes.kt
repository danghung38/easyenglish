package com.danghung.elearning.api.res

import java.io.Serializable

data class AuthRes(
    val token: String,
    val refreshToken: String,
    val expiredAt: Long
): Serializable