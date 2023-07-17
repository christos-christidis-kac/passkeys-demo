package com.christidischristos.passkeys.repository.webauthn

import com.christidischristos.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristos.passkeys.repository.ApiResult
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions

interface WebauthnRepository {

    suspend fun initWebauthnLogin(
        userId: String
    ): ApiResult<PublicKeyCredentialRequestOptions>

    suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun initWebauthnRegistration(): ApiResult<PublicKeyCredentialCreationOptions>

    suspend fun finalizeWebauthnRegistration(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun deleteWebauthnCredential(
        credentialId: String
    ): ApiResult<Unit>
}
