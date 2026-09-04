package com.rockbyte.lighton.di

import com.rockbyte.lighton.page.HomeViewModel
import com.rockbyte.lighton.repo.SettingsRepo
import com.rockbyte.lighton.repo.SettingsRepository
import com.rockbyte.lighton.store.SettingsStorage
import com.rockbyte.lighton.store.SettingsStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SettingsStore> { SettingsStorage(androidContext()) }
    single<SettingsRepo> { SettingsRepository(get()) }
    viewModel { HomeViewModel(get()) }
}
