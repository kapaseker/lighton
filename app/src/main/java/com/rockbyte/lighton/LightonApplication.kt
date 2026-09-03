package com.rockbyte.lighton

import android.app.Application
import com.rockbyte.lighton.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LightonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@LightonApplication)
            modules(appModule)
        }
    }
}
