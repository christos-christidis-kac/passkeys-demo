package com.christidischristos.passkeys.repository.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.christidischristos.passkeys.datastore.Constants.JWT_KEY
import com.christidischristos.passkeys.datastore.Constants.USER_ID_KEY
import com.christidischristos.passkeys.network.api.UserApi
import com.christidischristos.passkeys.network.model.CreateUserRequest
import com.christidischristos.passkeys.network.model.CreateUserResponse
import com.christidischristos.passkeys.network.model.GetUserByIdResponse
import com.christidischristos.passkeys.network.model.GetUserDetailsByEmailRequest
import com.christidischristos.passkeys.network.model.GetUserDetailsByEmailResponse
import com.christidischristos.passkeys.repository.ApiResult
import com.christidischristos.passkeys.repository.util.safeApiCall
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    private var userId: String? = null
    private var jwtKey: String? = null

    init {
        GlobalScope.launch {
            dataStore.data.collectLatest {
                userId = it[USER_ID_KEY]
                jwtKey = it[JWT_KEY]
            }
        }
    }

    override suspend fun getUserDetailsByEmail(email: String): ApiResult<GetUserDetailsByEmailResponse> {
        return safeApiCall {
            userApi.getUserDetailsByEmail(
                GetUserDetailsByEmailRequest(email)
            )
        }
    }

    override suspend fun createUser(email: String): ApiResult<CreateUserResponse> {
        return safeApiCall {
            userApi.createUser(
                CreateUserRequest(email)
            )
        }
    }

    override suspend fun getUserById(): ApiResult<GetUserByIdResponse> {
        return safeApiCall {
            userApi.getUserById(bearerToken = "Bearer $jwtKey", userId = userId!!)
        }
    }

    override suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

}
