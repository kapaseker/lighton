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

    override suspend fun save(
        brightness: Float,
        dotSize: Float,
        red: Float,
        green: Float,
        blue: Float,
        eyeCareIndex: Int,
    ) {
        saved = Settings(brightness, dotSize, red, green, blue, eyeCareIndex)
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
        repo.settingsFlow.value = Settings(brightness = 0.5f, dotSize = 100f, red = 1f, green = 0.5f, blue = 0f, eyeCareIndex = 2)
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1f, state.red, 0.001f)
        assertEquals(0.5f, state.green, 0.001f)
        assertEquals(0f, state.blue, 0.001f)
        assertEquals(2, state.eyeCareIndex)
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

    @Test
    fun eyeCareSelectUpdatesColorIndex() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.onEyeCareColorSelect(0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.eyeCareIndex)
        assertEquals(EyeCareColors[0].red, state.red, 0.001f)
        assertEquals(EyeCareColors[0].green, state.green, 0.001f)
        assertEquals(EyeCareColors[0].blue, state.blue, 0.001f)
        // 持久化统一由确认按钮触发，选中护眼色本身不写 store
        assertEquals(null, repo.saved)
    }

    @Test
    fun channelChangeClearsEyeCareSelection() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.onEyeCareColorSelect(3)
        viewModel.onGreenChange(0.5f)

        assertEquals(-1, viewModel.uiState.value.eyeCareIndex)
    }

    @Test
    fun eyeCareSelectWithInvalidIndexIsIgnored() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.onEyeCareColorSelect(EyeCareColors.size)

        assertEquals(-1, viewModel.uiState.value.eyeCareIndex)
    }

    @Test
    fun beginColorEditing_initializesUnsetColor() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.beginColorEditing()

        val state = viewModel.uiState.value
        assertEquals(1f, state.red, 0.001f)
        assertEquals(1f, state.green, 0.001f)
        assertEquals(1f, state.blue, 0.001f)
    }

    @Test
    fun confirmColorEditing_persistsEditedColor() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.beginColorEditing()
        viewModel.onEyeCareColorSelect(2)
        viewModel.confirmColorEditing()
        advanceUntilIdle()

        val saved = requireNotNull(repo.saved)
        assertEquals(2, saved.eyeCareIndex)
        assertEquals(EyeCareColors[2].red, saved.red, 0.001f)
        assertEquals(EyeCareColors[2].green, saved.green, 0.001f)
        assertEquals(EyeCareColors[2].blue, saved.blue, 0.001f)
    }

    @Test
    fun cancelColorEditing_restoresColorBeforeEditing() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        repo.settingsFlow.value = Settings(brightness = 0.5f, dotSize = 100f, red = 1f, green = 0.5f, blue = 0f, eyeCareIndex = 2)
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.beginColorEditing()
        viewModel.onEyeCareColorSelect(0)
        viewModel.onRedChange(0.1f)
        viewModel.cancelColorEditing()

        val state = viewModel.uiState.value
        assertEquals(1f, state.red, 0.001f)
        assertEquals(0.5f, state.green, 0.001f)
        assertEquals(0f, state.blue, 0.001f)
        assertEquals(2, state.eyeCareIndex)
        // 取消不写 store
        assertEquals(null, repo.saved)
    }

    @Test
    fun cancelColorEditing_restoresUnsetColorToUnset() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(FakeSettingsRepository())
        advanceUntilIdle()

        viewModel.beginColorEditing() // 未设置 → 初始化为白
        viewModel.onRedChange(0.2f)
        viewModel.cancelColorEditing()

        val state = viewModel.uiState.value
        assertEquals(-1f, state.red, 0.001f)
        assertEquals(-1f, state.green, 0.001f)
        assertEquals(-1f, state.blue, 0.001f)
    }
}
