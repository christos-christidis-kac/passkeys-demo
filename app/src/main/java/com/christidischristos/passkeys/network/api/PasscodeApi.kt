package com.christidischristos.passkeys.network.api

import com.christidischristos.passkeys.network.model.PasscodeLoginFinalizeRequest
import com.christidischristos.passkeys.network.model.InitPasscodeLoginRequest
import com.christidischristos.passkeys.network.model.PasscodeLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PasscodeApi {

    @POST("passcode/login/initialize")
    suspend fun initPasscodeLogin(
        @Body request: InitPasscodeLoginRequest
    ): PasscodeLoginResponse

    @POST("passcode/login/finalize")
    suspend fun finalizePasscodeLogin(
        @Body request: PasscodeLoginFinalizeRequest
    ): Response<PasscodeLoginResponse>
}
