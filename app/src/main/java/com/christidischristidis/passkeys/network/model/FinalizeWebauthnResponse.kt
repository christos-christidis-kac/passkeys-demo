package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinalizeWebauthnResponse(
    @SerialName("credential_id")
    val credentialId: String,
    @SerialName("user_id")
    val userId: String
)
