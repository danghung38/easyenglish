package com.danghung.elearning.api.res

import java.io.Serializable

data class AuthRes(
    val authenticated: Boolean = false,
    val requires2fa: Boolean = false,
    val tempToken: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: Long? = null,
    val role: String? = null,
    val username: String? = null,
    val fullName: String? = null
): Serializable
