package com.christidischristidis.passkeys.repository.di

import com.christidischristidis.passkeys.repository.passcode.PasscodeRepository
import com.christidischristidis.passkeys.repository.passcode.PasscodeRepositoryImpl
import com.christidischristidis.passkeys.repository.user.UserRepository
import com.christidischristidis.passkeys.repository.user.UserRepositoryImpl
import com.christidischristidis.passkeys.repository.webauthn.WebauthnRepository
import com.christidischristidis.passkeys.repository.webauthn.WebauthnRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun userRepository(repository: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun passcodeRepository(repository: PasscodeRepositoryImpl): PasscodeRepository

    @Singleton
    @Binds
    fun webauthnRepository(repository: WebauthnRepositoryImpl): WebauthnRepository
}
