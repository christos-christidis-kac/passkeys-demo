package com.christidischristos.passkeys.repository.passcode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.christidischristos.passkeys.datastore.Constants.JWT_KEY
import com.christidischristos.passkeys.datastore.Constants.PASSCODE_INIT_ID_KEY
import com.christidischristos.passkeys.datastore.Constants.USER_ID_KEY
import com.christidischristos.passkeys.network.api.PasscodeApi
import com.christidischristos.passkeys.network.model.InitPasscodeLoginRequest
import com.christidischristos.passkeys.network.model.PasscodeLoginFinalizeRequest
import com.christidischristos.passkeys.network.model.PasscodeLoginResponse
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.util.safeApiCall
import com.christidischristos.passkeys.repository.util.throwUnauthorized
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class PasscodeRepositoryImpl @Inject constructor(
    private val passcodeApi: PasscodeApi,
    private val dataStore: DataStore<Preferences>
) : PasscodeRepository {

    private var passcodeInitId: String? = null

    init {
        GlobalScope.launch {
            dataStore.data.collectLatest {
                passcodeInitId = it[PASSCODE_INIT_ID_KEY]
            }
        }
    }

    override suspend fun initPasscodeLogin(
        userId: String,
        emailId: String
    ): ApiResult<PasscodeLoginResponse> {
        val result = safeApiCall {
            passcodeApi.initPasscodeLogin(
                InitPasscodeLoginRequest(userId = userId, emailId = emailId)
            )
        }
        if (result is ApiResult.Success) {
            dataStore.edit { prefs ->
                prefs[USER_ID_KEY] = userId
                prefs[PASSCODE_INIT_ID_KEY] = result.data.id
            }
        }
        return result
    }

    override suspend fun finalizePasscodeLogin(
        passcode: String
    ): ApiResult<PasscodeLoginResponse> {
        return safeApiCall {
            val apiResponse = passcodeApi.finalizePasscodeLogin(
                PasscodeLoginFinalizeRequest(passcodeInitId = passcodeInitId!!, code = passcode)
            )
            if (apiResponse.isSuccessful) {
                val jwt = apiResponse.headers()["X-Auth-Token"] ?: throwUnauthorized()
                dataStore.edit { prefs ->
                    prefs[JWT_KEY] = jwt
                }
            }
            apiResponse.body() ?: throwUnauthorized()
        }
    }
}
