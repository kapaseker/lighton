package com.rockbyte.lighton.di

import android.content.Context
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class AppModuleTest {
    @Test
    fun appModuleDefinesEveryConstructorDependency() {
        appModule.verify(extraTypes = listOf(Context::class))
    }
}
