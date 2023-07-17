package com.christidischristidis.passkeys.screen.passkey

import kotlinx.serialization.Serializable

@Serializable
data class CredentialManagerCreatedKeyData(
    val response: Response,
    val authenticatorAttachment: String,
    val id: String,
    val rawId: String,
    val type: String
) {
    @Serializable
    data class Response(
        val clientDataJSON: String,
        val attestationObject: String,
        val transports: List<String>
    )
}
