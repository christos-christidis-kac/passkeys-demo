package com.christidischristidis.passkeys.repository.webauthn

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.christidischristidis.passkeys.datastore.Constants.JWT_KEY
import com.christidischristidis.passkeys.network.api.WebauthnApi
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnLoginResponse
import com.christidischristidis.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnLoginRequest
import com.christidischristidis.passkeys.network.model.InitWebauthnRegistrationRequest
import com.christidischristidis.passkeys.repository.ApiResult
import com.christidischristidis.passkeys.repository.util.safeApiCall
import com.christidischristidis.passkeys.repository.util.throwUnauthorized
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions
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
                    Base64.DEFAULT or Base64.URL_SAFE
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
    ): ApiResult<FinalizeWebauthnLoginResponse> {
        require(credential.response !is AuthenticatorErrorResponse) {
            "credential.response must not contain AuthenticatorErrorResponse"
        }

        val authenticatorResponse = credential.response as AuthenticatorAssertionResponse

        val apiRequest = FinalizeWebauthnLoginRequest(
            id = credential.id,
            rawId = credential.id,
            type = credential.type,
            response = FinalizeWebauthnLoginRequest.Response(
                clientDataJson = toBase64WithFlags(authenticatorResponse.clientDataJSON),
                authenticatorData = toBase64WithFlags(authenticatorResponse.authenticatorData),
                signature = toBase64WithFlags(authenticatorResponse.signature),
                userHandle = authenticatorResponse.userHandle?.let {
                    toBase64WithFlags(it)
                }
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

    override suspend fun initWebauthnRegistration(
        request: InitWebauthnRegistrationRequest
    ): ApiResult<Unit> {
        return safeApiCall {
            webauthnApi.initWebauthnRegistration(
                bearerToken = "Bearer $jwtKey",
                request = request
            )
        }
    }

    override suspend fun finalizeWebauthnRegistration(
        request: FinalizeWebauthnRegistrationRequest
    ): ApiResult<Unit> {
        return safeApiCall {
            webauthnApi.finalizeWebauthnRegistration(
                bearerToken = "Bearer $jwtKey",
                request = request
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

private fun toBase64WithFlags(byteArray: ByteArray): String {
    return Base64.encodeToString(
        byteArray,
        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    )
}
