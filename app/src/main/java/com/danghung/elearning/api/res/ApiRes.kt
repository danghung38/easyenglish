package com.danghung.elearning.api.res

import java.io.Serializable

data class ApiRes<T>(
    val code: Int,
    val result: T?
): Serializable