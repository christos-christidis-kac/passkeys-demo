package com.christidischristidis.passkeys.repository.user

import com.christidischristidis.passkeys.network.model.CreateUserResponse
import com.christidischristidis.passkeys.network.model.GetUserDetailsByEmailResponse
import com.christidischristidis.passkeys.network.model.GetUserByIdResponse
import com.christidischristidis.passkeys.repository.ApiResult

interface UserRepository {

    suspend fun getUserDetailsByEmail(
        email: String
    ): ApiResult<GetUserDetailsByEmailResponse>

    suspend fun createUser(
        email: String
    ): ApiResult<CreateUserResponse>

    suspend fun getUserById(): ApiResult<GetUserByIdResponse>

    suspend fun logout()
}
