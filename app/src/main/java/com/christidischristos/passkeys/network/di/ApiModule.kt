package com.christidischristos.passkeys.network.di

import com.christidischristos.passkeys.network.api.PasscodeApi
import com.christidischristos.passkeys.network.api.UserApi
import com.christidischristos.passkeys.network.api.WebauthnApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Singleton
    @Provides
    fun userApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun passcodeApi(retrofit: Retrofit): PasscodeApi {
        return retrofit.create(PasscodeApi::class.java)
    }

    @Singleton
    @Provides
    fun webauthnApi(retrofit: Retrofit): WebauthnApi {
        return retrofit.create(WebauthnApi::class.java)
    }
}
