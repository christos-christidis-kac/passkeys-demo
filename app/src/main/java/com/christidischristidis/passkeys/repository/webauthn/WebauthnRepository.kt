package com.christidischristidis.passkeys.repository.webauthn

import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationResponse
import com.christidischristidis.passkeys.repository.ApiResult
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions

interface WebauthnRepository {

    suspend fun initWebauthnLogin(
        userId: String
    ): ApiResult<PublicKeyCredentialRequestOptions>

    suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun initWebauthnRegistration(): ApiResult<InitWebauthnRegistrationResponse>

    suspend fun finalizeWebauthnRegistration(
        credentialManagerRegistrationJson: String
    ): ApiResult<FinalizeWebauthnResponse>

    suspend fun deleteWebauthnCredential(
        credentialId: String
    ): ApiResult<Unit>
}
