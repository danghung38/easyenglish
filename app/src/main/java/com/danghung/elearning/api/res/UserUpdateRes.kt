package com.danghung.elearning.api.res

import java.io.Serializable

data class UserUpdateRes(
    val fullName: String,
    val gender: String,
    val dob: String,
    val bandsTarget: Double
): Serializable