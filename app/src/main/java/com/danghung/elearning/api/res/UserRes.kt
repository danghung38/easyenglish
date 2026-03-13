package com.danghung.elearning.api.res

import java.io.Serializable

data class UserRes(
    val username: String?,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val dob: String?,
    val bandsTarget: Double?,
    val gender: String?,
    val avatar: String?
): Serializable