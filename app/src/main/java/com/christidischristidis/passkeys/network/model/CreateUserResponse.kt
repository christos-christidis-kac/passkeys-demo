package com.christidischristidis.passkeys.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponse(
    @SerialName("user_id")
    val userId: String,
    @SerialName("email_id")
    val emailId: String
)
