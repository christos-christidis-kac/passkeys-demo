package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserDetailsByEmailResponse(
    @SerialName("id")
    val userId: String,
    @SerialName("email_id")
    val emailId: String,
    val verified: Boolean,
    @SerialName("has_webauthn_credential")
    val hasWebauthnCredential: Boolean
)
