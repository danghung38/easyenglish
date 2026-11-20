package com.danghung.elearning.api.req

import java.io.Serializable

data class RefreshReq(
    val token: String
): Serializable