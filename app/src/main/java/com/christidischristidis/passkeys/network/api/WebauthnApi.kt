package com.christidischristidis.passkeys.network.api

import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristidis.passkeys.network.model.InitWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnLoginResponse
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface WebauthnApi {

    @POST("webauthn/login/initialize")
    suspend fun initWebauthnLogin(
        @Body body: InitWebauthnLoginRequest
    ): InitWebauthnLoginResponse

    @POST("webauthn/login/finalize")
    suspend fun finalizeWebauthnLogin(
        @Body body: FinalizeWebauthnLoginRequest
    ): Response<FinalizeWebauthnResponse>

    @POST("webauthn/registration/initialize")
    suspend fun initWebauthnRegistration(
        @Header("Authorization") bearerToken: String
    ): InitWebauthnRegistrationResponse

    @POST("webauthn/registration/finalize")
    suspend fun finalizeWebauthnRegistration(
        @Header("Authorization") bearerToken: String,
        @Body body: FinalizeWebauthnRegistrationRequest
    ): FinalizeWebauthnResponse

    @DELETE("webauthn/credentials/{id}")
    suspend fun deleteWebauthnCredential(
        @Header("Authorization") bearerToken: String,
        @Path("id") credentialId: String
    )
}
