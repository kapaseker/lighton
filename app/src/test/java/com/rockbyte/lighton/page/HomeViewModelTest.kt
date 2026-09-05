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
    val settingsFlow = MutableStateFlow(Settings(brightness = -1f, dotSize = 0f))
    var saved: Settings? = null
        private set

    override val settings: Flow<Settings> = settingsFlow

    override suspend fun save(brightness: Float, dotSize: Float, red: Float, green: Float, blue: Float) {
        saved = Settings(brightness, dotSize, red, green, blue)
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
    fun loadsPersistedColorOnStart() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        repo.settingsFlow.value = Settings(brightness = 0.5f, dotSize = 100f, red = 1f, green = 0.5f, blue = 0f)
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1f, state.red, 0.001f)
        assertEquals(0.5f, state.green, 0.001f)
        assertEquals(0f, state.blue, 0.001f)
    }

    @Test
    fun channelChangeUpdatesUiState() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.onRedChange(0.2f)
        viewModel.onGreenChange(0.4f)
        viewModel.onBlueChange(0.6f)

        val state = viewModel.uiState.value
        assertEquals(0.2f, state.red, 0.001f)
        assertEquals(0.4f, state.green, 0.001f)
        assertEquals(0.6f, state.blue, 0.001f)
    }

    @Test
    fun initColorIfUnset_setsWhiteOnlyOnce() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.initColorIfUnset()
        viewModel.onRedChange(0.3f)

        // 已设置过颜色（含用户拖动后），再次进入取色模式不得重置
        viewModel.initColorIfUnset()

        val state = viewModel.uiState.value
        assertEquals(0.3f, state.red, 0.001f)
        assertEquals(1f, state.green, 0.001f)
        assertEquals(1f, state.blue, 0.001f)
    }

    @Test
    fun savePersistsCurrentColor() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.initColorIfUnset()
        viewModel.onGreenChange(0.5f)
        viewModel.save()
        advanceUntilIdle()

        val saved = requireNotNull(repo.saved)
        assertEquals(1f, saved.red, 0.001f)
        assertEquals(0.5f, saved.green, 0.001f)
        assertEquals(1f, saved.blue, 0.001f)
    }
}
