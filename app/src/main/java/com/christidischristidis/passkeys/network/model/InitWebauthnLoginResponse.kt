package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class InitWebauthnLoginResponse(
    val publicKey: PublicKey
) {
    @Serializable
    data class PublicKey(
        val challenge: String,
        val timeout: Long?,
        val rpId: String,
        val allowCredentials: List<AllowCredential>? = null,
        val userVerification: String? = null
    ) {
        @Serializable
        data class AllowCredential(
            val type: String,
            val id: String
        )
    }
}
