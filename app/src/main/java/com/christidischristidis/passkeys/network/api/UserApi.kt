package com.christidischristidis.passkeys.network.api

import com.christidischristidis.passkeys.network.model.CreateUserRequest
import com.christidischristidis.passkeys.network.model.CreateUserResponse
import com.christidischristidis.passkeys.network.model.GetUserByIdResponse
import com.christidischristidis.passkeys.network.model.GetUserDetailsByEmailRequest
import com.christidischristidis.passkeys.network.model.GetUserDetailsByEmailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @POST("user")
    suspend fun getUserDetailsByEmail(
        @Body request: GetUserDetailsByEmailRequest
    ): GetUserDetailsByEmailResponse

    @POST("users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): CreateUserResponse

    @GET("users/{id}")
    suspend fun getUserById(
        @Header("Authorization") bearerToken: String,
        @Path("id") userId: String,
    ): GetUserByIdResponse
}

