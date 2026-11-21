package com.danghung.elearning.api.res

import java.io.Serializable

data class TwoFactorSetupRes(
    val secretKey: String? = null,
    val qrCodeUri: String? = null
) : Serializable
