package com.christidischristos.passkeys.repository.webauthn

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.christidischristos.passkeys.datastore.Constants.JWT_KEY
import com.christidischristos.passkeys.network.api.WebauthnApi
import com.christidischristos.passkeys.network.model.FinalizeWebauthnLoginRequest
import com.christidischristos.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristos.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristos.passkeys.network.model.InitWebauthnLoginRequest
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.util.safeApiCall
import com.christidischristos.passkeys.repository.util.throwUnauthorized
import com.google.android.gms.fido.fido2.api.common.Attachment
import com.google.android.gms.fido.fido2.api.common.AttestationConveyancePreference
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity
import com.google.android.gms.fido.fido2.api.common.ResidentKeyRequirement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class WebauthnRepositoryImpl @Inject constructor(
    private val webauthnApi: WebauthnApi,
    private val dataStore: DataStore<Preferences>
) : WebauthnRepository {

    private var jwtKey: String? = null

    init {
        GlobalScope.launch {
            dataStore.data.collectLatest {
                jwtKey = it[JWT_KEY]
            }
        }
    }

    override suspend fun initWebauthnLogin(
        userId: String
    ): ApiResult<PublicKeyCredentialRequestOptions> {
        return safeApiCall {
            val apiResponse = webauthnApi.initWebauthnLogin(
                InitWebauthnLoginRequest(userId)
            )
            val builder = PublicKeyCredentialRequestOptions.Builder()
            builder.setChallenge(
                Base64.decode(
                    apiResponse.publicKey.challenge,
                    Base64.URL_SAFE
                )
            )
            builder.setTimeoutSeconds(apiResponse.publicKey.timeout?.toDouble())
            builder.setRpId(apiResponse.publicKey.rpId)
            builder.setAllowList(null)

            builder.build()
        }
    }

    override suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse> {
        require(credential.response !is AuthenticatorErrorResponse) {
            "credential.response must not contain AuthenticatorErrorResponse"
        }

        val authenticatorResponse = credential.response as AuthenticatorAssertionResponse

        val apiRequest = FinalizeWebauthnLoginRequest(
            id = credential.id,
            rawId = credential.id,
            type = credential.type,
            response = FinalizeWebauthnLoginRequest.Response(
                clientDataJson = authenticatorResponse.clientDataJSON.toBase64WithFlags(),
                authenticatorData = authenticatorResponse.authenticatorData.toBase64WithFlags(),
                signature = authenticatorResponse.signature.toBase64WithFlags(),
                userHandle = authenticatorResponse.userHandle?.toBase64WithFlags()
            )
        )

        return safeApiCall {
            val apiResponse = webauthnApi.finalizeWebauthnLogin(apiRequest)
            if (apiResponse.isSuccessful) {
                val jwt = apiResponse.headers()["X-Auth-Token"] ?: throwUnauthorized()
                dataStore.edit { prefs ->
                    prefs[JWT_KEY] = jwt
                }
            }
            apiResponse.body() ?: throwUnauthorized()
        }
    }

    override suspend fun initWebauthnRegistration(): ApiResult<PublicKeyCredentialCreationOptions> {
        return safeApiCall {
            val publicKey = webauthnApi.initWebauthnRegistration(
                bearerToken = "Bearer $jwtKey"
            ).publicKey
            val user = PublicKeyCredentialUserEntity(
                Base64.decode(
                    publicKey.user.id,
                    Base64.URL_SAFE
                ),
                publicKey.user.name,
                "",
                publicKey.user.displayName ?: publicKey.user.name
            )

            val rp = PublicKeyCredentialRpEntity(
                publicKey.rp.id, publicKey.rp.name, null
            )

            val credParams = publicKey.pubKeyCredParams.mapNotNull {
                if (it.alg != -8) { // workaround, because android does not support this algorithm
                    PublicKeyCredentialParameters(it.type, it.alg)
                } else {
                    null
                }
            }

            val criteriaBuilder = publicKey.authenticatorSelection?.let {
                val criteriaBuilder = AuthenticatorSelectionCriteria.Builder()
                it.authenticatorAttachment?.let { at ->
                    criteriaBuilder.setAttachment(
                        Attachment.fromString(at)
                    )
                }
                criteriaBuilder.setRequireResidentKey(it.requireResidentKey)
                it.residentKey?.let { rk ->
                    criteriaBuilder.setResidentKeyRequirement(
                        ResidentKeyRequirement.fromString(rk)
                    )
                }
                it.userVerification?.let { uv ->
                    criteriaBuilder.setResidentKeyRequirement(
                        ResidentKeyRequirement.fromString(uv)
                    )
                }
            }

            val builder = PublicKeyCredentialCreationOptions.Builder()
            builder.setChallenge(
                Base64.decode(
                    publicKey.challenge,
                    Base64.URL_SAFE
                )
            )
            builder.setTimeoutSeconds(publicKey.timeout?.toDouble())
            builder.setAttestationConveyancePreference(
                publicKey.attestation?.let {
                    AttestationConveyancePreference.fromString(it)
                }
            )
            builder.setUser(user)
            builder.setRp(rp)
            builder.setParameters(credParams)
            builder.setAuthenticatorSelection(criteriaBuilder?.build())

            builder.build()
        }
    }

    override suspend fun finalizeWebauthnRegistration(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse> {
        require(credential.response !is AuthenticatorErrorResponse) {
            "credential.response must not contain AuthenticatorErrorResponse"
        }

        val authenticatorResponse = credential.response as AuthenticatorAttestationResponse

        val apiRequest = FinalizeWebauthnRegistrationRequest(
            id = credential.id,
            rawId = credential.id,
            type = credential.type,
            response = FinalizeWebauthnRegistrationRequest.Response(
                clientDataJSON = authenticatorResponse.clientDataJSON.toBase64WithFlags(),
                attestationObject = authenticatorResponse.attestationObject.toBase64WithFlags()
            ),
            transports = authenticatorResponse.transports.toList()
        )

        return safeApiCall {
            webauthnApi.finalizeWebauthnRegistration(
                bearerToken = "Bearer $jwtKey",
                body = apiRequest
            )
        }
    }

    override suspend fun deleteWebauthnCredential(credentialId: String): ApiResult<Unit> {
        return safeApiCall {
            webauthnApi.deleteWebauthnCredential(
                bearerToken = "Bearer $jwtKey",
                credentialId = credentialId
            )
        }
    }
}

private fun ByteArray.toBase64WithFlags(): String {
    return Base64.encodeToString(
        this,
        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    )
}
