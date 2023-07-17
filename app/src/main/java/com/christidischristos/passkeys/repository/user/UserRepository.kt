package com.christidischristos.passkeys.repository.user

import com.christidischristos.passkeys.network.model.CreateUserResponse
import com.christidischristos.passkeys.network.model.GetUserDetailsByEmailResponse
import com.christidischristos.passkeys.network.model.GetUserByIdResponse
import com.christidischristos.passkeys.repository.ApiResult

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
