package com.christidischristos.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class InitWebauthnRegistrationResponse(
    val publicKey: PublicKey
) {
    @Serializable
    data class PublicKey(
        val rp: RelyingParty,
        val user: User,
        val challenge: String,
        val pubKeyCredParams: List<CredParam>,
        val timeout: Long? = null,
        val authenticatorSelection: AuthenticatorSelection? = null,
        val attestation: String? = null
    ) {
        @Serializable
        data class RelyingParty(
            val name: String,
            val id: String
        )

        @Serializable
        data class User(
            val id: String,
            val name: String,
            val displayName: String? = null
        )

        @Serializable
        data class CredParam(
            val type: String,
            val alg: Int
        )

        @Serializable
        data class AuthenticatorSelection(
            val authenticatorAttachment: String? = null,
            val requireResidentKey: Boolean? = null,
            val residentKey: String? = null,
            val userVerification: String? = null
        )
    }
}
