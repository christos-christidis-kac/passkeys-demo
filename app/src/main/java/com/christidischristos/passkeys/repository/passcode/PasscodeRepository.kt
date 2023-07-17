package com.christidischristos.passkeys.repository.passcode

import com.christidischristos.passkeys.network.model.PasscodeLoginResponse
import com.christidischristos.passkeys.repository.ApiResult

interface PasscodeRepository {

    suspend fun initPasscodeLogin(
        userId: String,
        emailId: String
    ): ApiResult<PasscodeLoginResponse>

    suspend fun finalizePasscodeLogin(
        passcode: String
    ): ApiResult<PasscodeLoginResponse>
}
