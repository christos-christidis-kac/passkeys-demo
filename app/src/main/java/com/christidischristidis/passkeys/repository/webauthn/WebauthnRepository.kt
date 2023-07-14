package com.christidischristidis.passkeys.repository.webauthn

import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginResponse
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationRequest
import com.christidischristidis.passkeys.repository.ApiResult
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions

interface WebauthnRepository {

    suspend fun initWebauthnLogin(
        userId: String
    ): ApiResult<PublicKeyCredentialRequestOptions>

    suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnLoginResponse>

    suspend fun initWebauthnRegistration(
        request: InitWebauthnRegistrationRequest
    ): ApiResult<Unit>

    suspend fun finalizeWebauthnRegistration(
        request: FinalizeWebauthnRegistrationRequest
    ): ApiResult<Unit>

    suspend fun deleteWebauthnCredential(
        credentialId: String
    ): ApiResult<Unit>
}
