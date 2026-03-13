package com.danghung.elearning.api.res

import java.io.Serializable

data class ApiErrorRes(
    val code: Int,
    val message: String,
    val result: String?
): Serializable