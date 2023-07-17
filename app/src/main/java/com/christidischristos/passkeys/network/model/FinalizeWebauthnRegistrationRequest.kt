package com.christidischristos.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FinalizeWebauthnRegistrationRequest(
    val id: String,
    val rawId: String,
    val type: String,
    val response: Response,
    val transports: List<String>
) {
    @Serializable
    data class Response(
        val clientDataJSON: String,
        val attestationObject: String,
    )
}
