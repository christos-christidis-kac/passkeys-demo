package com.christidischristos.passkeys.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitWebauthnLoginRequest(
    @SerialName("user_id")
    val userId: String
)
