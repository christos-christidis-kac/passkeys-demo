package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.SerialName
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
        val userVerification: UserVerification
    ) {
        @Serializable
        data class AllowCredential(
            val type: String,
            val id: String
        )

        enum class UserVerification {
            @SerialName("required")
            REQUIRED,

            @SerialName("preferred")
            PREFERRED,

            @SerialName("discouraged")
            DISCOURAGED
        }
    }
}
