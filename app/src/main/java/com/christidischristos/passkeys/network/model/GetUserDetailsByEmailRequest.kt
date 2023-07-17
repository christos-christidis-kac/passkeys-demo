package com.christidischristos.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GetUserDetailsByEmailRequest(
    val email: String
)
