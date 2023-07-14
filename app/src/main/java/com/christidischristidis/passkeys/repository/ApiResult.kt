package com.christidischristidis.passkeys.repository

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class HttpError(val code: Int, val msg: String) : ApiResult<Nothing>
    object NetworkError : ApiResult<Nothing>
    data class GeneralError(val msg: String?) : ApiResult<Nothing>
}
