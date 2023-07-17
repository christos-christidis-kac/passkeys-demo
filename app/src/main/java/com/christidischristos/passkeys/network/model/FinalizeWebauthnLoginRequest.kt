package com.christidischristos.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FinalizeWebauthnLoginRequest(
    val id: String,
    val rawId: String,
    val type: String,
    val response: Response
) {
    @Serializable
    data class Response(
        val clientDataJson: String,
        val authenticatorData: String,
        val signature: String,
        val userHandle: String?
    )
}
