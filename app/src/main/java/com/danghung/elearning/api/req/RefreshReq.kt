package com.danghung.elearning.api.req

import java.io.Serializable

data class RefreshReq(
    val refreshToken: String
): Serializable