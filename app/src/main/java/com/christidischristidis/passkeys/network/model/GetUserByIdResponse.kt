package com.christidischristidis.passkeys.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserByIdResponse(
    @SerialName("id")
    val userId: String,
    val email: String,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
    @SerialName("webauthn_credentials")
    val webauthnCredentials: List<WebauthCredential>? = null
) {
    @Serializable
    data class WebauthCredential(
        val id: String
    )
}
