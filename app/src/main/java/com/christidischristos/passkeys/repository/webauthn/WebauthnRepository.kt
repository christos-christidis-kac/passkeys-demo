package com.christidischristos.passkeys.repository.webauthn

import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.christidischristos.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristos.passkeys.network.model.MyCreatePublicKeyResponse
import com.christidischristos.passkeys.repository.ApiResult

interface WebauthnRepository {

    suspend fun initWebauthnLogin(
        userId: String
    ): ApiResult<GetPublicKeyCredentialOption>

    suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun initWebauthnRegistration(): ApiResult<CreatePublicKeyCredentialRequest>

    suspend fun finalizeWebauthnRegistration(
        createPublicKeyResponse: MyCreatePublicKeyResponse
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun deleteWebauthnCredential(
        credentialId: String
    ): ApiResult<Unit>
}
