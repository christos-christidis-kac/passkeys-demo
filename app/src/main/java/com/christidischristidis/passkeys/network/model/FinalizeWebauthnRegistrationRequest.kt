package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FinalizeWebauthnRegistrationRequest(
    val id: String,
    val rawId: String,
    val type: String,
    val response: Response
) {
    @Serializable
    data class Response(
        val clientDataJSON: String,
        val attestationObject: String,
        val transports: List<String>
    )
}
