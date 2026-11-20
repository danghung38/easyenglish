package com.danghung.elearning.api.req

import java.io.Serializable

data class UserCreationReq(
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val gender: String,
    val dob: String
): Serializable