package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GetUserDetailsByEmailRequest(
    val email: String
)
