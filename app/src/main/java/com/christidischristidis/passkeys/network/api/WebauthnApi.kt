package com.christidischristidis.passkeys.network.api

import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginResponse
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnLoginResponse
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface WebauthnApi {

    @POST("webauthn/login/initialize")
    suspend fun initWebauthnLogin(
        @Body request: InitWebauthnLoginRequest
    ): InitWebauthnLoginResponse

    @POST("webauthn/login/finalize")
    suspend fun finalizeWebauthnLogin(
        @Body request: FinalizeWebauthnLoginRequest
    ): Response<FinalizeWebauthnLoginResponse>

    @POST("webauthn/registration/initialize")
    suspend fun initWebauthnRegistration(
        @Header("Authorization") bearerToken: String,
        @Body request: InitWebauthnRegistrationRequest
    )

    @POST("webauthn/registration/finalize")
    suspend fun finalizeWebauthnRegistration(
        @Header("Authorization") bearerToken: String,
        @Body request: FinalizeWebauthnRegistrationRequest
    )

    @DELETE("webauthn/credentials/{id}")
    suspend fun deleteWebauthnCredential(
        @Header("Authorization") bearerToken: String,
        @Path("id") credentialId: String
    )
}
