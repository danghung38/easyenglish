package com.danghung.elearning.api.req

import java.io.Serializable

data class UserUpdateReq(
    val fullName: String,
    val gender: String,
    val dob: String,
    val bandsTarget: Double?
): Serializable