package com.danghung.elearning.api.req

import java.io.Serializable

data class GoogleLoginReq(
    val idToken: String
) : Serializable
