package com.rockbyte.lighton.page

import com.rockbyte.lighton.repo.SettingsRepo
import com.rockbyte.lighton.store.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSettingsRepository : SettingsRepo {
    val settingsFlow = MutableStateFlow(Settings(brightness = -1f, dotSize = 0f, hue = -1f))
    var saved: Settings? = null
        private set

    override val settings: Flow<Settings> = settingsFlow

    override suspend fun save(brightness: Float, dotSize: Float, hue: Float) {
        saved = Settings(brightness, dotSize, hue)
    }
}

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsPersistedHueOnStart() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        repo.settingsFlow.value = Settings(brightness = 0.5f, dotSize = 100f, hue = 210f)
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        assertEquals(210f, viewModel.uiState.value.hue)
    }

    @Test
    fun hueChangeUpdatesUiState() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.onHueChange(120f)

        assertEquals(120f, viewModel.uiState.value.hue)
    }

    @Test
    fun savePersistsCurrentHue() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.onHueChange(300f)
        viewModel.save()
        advanceUntilIdle()

        assertEquals(300f, repo.saved?.hue)
    }
}
