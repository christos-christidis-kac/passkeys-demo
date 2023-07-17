package com.christidischristos.passkeys.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasscodeLoginFinalizeRequest(
    @SerialName("id")
    val passcodeInitId: String,
    val code: String
)
