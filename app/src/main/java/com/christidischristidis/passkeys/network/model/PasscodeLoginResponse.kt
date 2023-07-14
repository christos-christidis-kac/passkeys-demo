package com.christidischristidis.passkeys.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasscodeLoginResponse(
    val id: String,
    val ttl: Long,
    @SerialName("created_at")
    val createdAt: Instant
)
