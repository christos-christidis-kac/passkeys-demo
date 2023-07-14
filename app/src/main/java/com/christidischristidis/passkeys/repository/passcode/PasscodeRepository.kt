package com.christidischristidis.passkeys.repository.passcode

import com.christidischristidis.passkeys.network.model.PasscodeLoginResponse
import com.christidischristidis.passkeys.repository.ApiResult

interface PasscodeRepository {

    suspend fun initPasscodeLogin(
        userId: String,
        emailId: String
    ): ApiResult<PasscodeLoginResponse>

    suspend fun finalizePasscodeLogin(
        passcode: String
    ): ApiResult<PasscodeLoginResponse>
}
