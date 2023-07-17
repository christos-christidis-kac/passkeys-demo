package com.christidischristos.passkeys.network.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String
)
