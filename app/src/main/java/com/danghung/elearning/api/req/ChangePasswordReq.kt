package com.danghung.elearning.api.req

import java.io.Serializable

data class ChangePasswordReq(
    val oldPassword: String,
    val newPassword: String
): Serializable