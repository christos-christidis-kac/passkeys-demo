package com.christidischristidis.passkeys.repository.util

import com.christidischristidis.passkeys.repository.ApiResult
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (throwable: Throwable) {
        println(throwable.localizedMessage)
        when (throwable) {
            is IOException -> ApiResult.NetworkError
            is HttpException -> ApiResult.HttpError(
                throwable.code(),
                "${throwable.code()} ${throwable.message()}"
            )

            else -> ApiResult.GeneralError(throwable.message)
        }
    }
}

fun throwUnauthorized(): Nothing {
    throw HttpException(
        Response.error<ResponseBody>(401, "".toResponseBody()) // FIXME
    )
}
