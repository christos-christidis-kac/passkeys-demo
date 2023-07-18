package com.christidischristos.passkeys.repository.webauthn

import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.christidischristos.passkeys.datastore.Constants.JWT_KEY
import com.christidischristos.passkeys.network.api.WebauthnApi
import com.christidischristos.passkeys.network.model.FinalizeWebauthnRegistrationRequest
import com.christidischristos.passkeys.network.model.FinalizeWebauthnResponse
import com.christidischristos.passkeys.network.model.InitWebauthnLoginRequest
import com.christidischristos.passkeys.network.model.MyCreatePublicKeyResponse
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.util.safeApiCall
import com.christidischristos.passkeys.repository.util.throwUnauthorized
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    ): ApiResult<GetPublicKeyCredentialOption> {
        return safeApiCall {
            val apiResponse = webauthnApi.initWebauthnLogin(
                InitWebauthnLoginRequest(userId)
            )
            GetPublicKeyCredentialOption(
                requestJson = Json.encodeToString(apiResponse.publicKey),
                clientDataHash = null,
                preferImmediatelyAvailableCredentials = true
            )
        }
    }

    override suspend fun finalizeWebauthnLogin(
        credential: PublicKeyCredential
    ): ApiResult<FinalizeWebauthnResponse> {
        return safeApiCall {
            val apiResponse =
                webauthnApi.finalizeWebauthnLogin(credential.authenticationResponseJson)
            if (apiResponse.isSuccessful) {
                val jwt = apiResponse.headers()["X-Auth-Token"] ?: throwUnauthorized()
                dataStore.edit { prefs ->
                    prefs[JWT_KEY] = jwt
                }
            }
            apiResponse.body() ?: throwUnauthorized()
        }
    }

    override suspend fun initWebauthnRegistration(): ApiResult<CreatePublicKeyCredentialRequest> {
        return safeApiCall {
            val publicKey = webauthnApi.initWebauthnRegistration(
                bearerToken = "Bearer $jwtKey"
            ).publicKey
            val publicKeyAsJson = Json.encodeToString(publicKey)
            CreatePublicKeyCredentialRequest(publicKeyAsJson)
        }
    }

    override suspend fun finalizeWebauthnRegistration(
        createPublicKeyResponse: MyCreatePublicKeyResponse
    ): ApiResult<FinalizeWebauthnResponse> {
        val apiRequest = createPublicKeyResponse.let {
            FinalizeWebauthnRegistrationRequest(
                id = it.id,
                rawId = it.id,
                type = it.type,
                response = FinalizeWebauthnRegistrationRequest.Response(
                    clientDataJSON = it.response.clientDataJSON,
                    attestationObject = it.response.attestationObject
                ),
                transports = it.response.transports.filter { tr ->
                    tr in hankoSupportedTransports
                }
            )
        }

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

private val hankoSupportedTransports = listOf("usb", "nfc", "ble", "internal")
