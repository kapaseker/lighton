package com.rockbyte.lighton.page

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rockbyte.lighton.MainActivity
import com.rockbyte.lighton.store.settingsDataStoreForTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// 首页九宫格手势与色相圆环的 UI 测试：走真实 App 栈（真实 DataStore），用例前后清空设置
@RunWith(AndroidJUnit4::class)
class HomePageUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearSettingsAndSettle() {
        runBlocking { targetContext.settingsDataStoreForTest.edit { it.clear() } }
        // 清空会触发默认值回填（圆点回到最小尺寸），等待状态收敛后再断言
        composeRule.waitUntil(5_000) { dotWidthPx() <= minDotPx() + 1f }
    }

    @After
    fun clearSettings() {
        runBlocking { targetContext.settingsDataStoreForTest.edit { it.clear() } }
        composeRule.waitForIdle()
    }

    @Test
    fun dotIsShownOnLaunch() {
        composeRule.onNodeWithTag("dot").assertExists()
    }

    @Test
    fun bottomRowDragGrowsDot() {
        val before = dotWidthPx()
        composeRule.onNodeWithTag("homeRoot").performTouchInput {
            down(Offset(width * 0.5f, height * 0.9f))
            // 跨过 touchSlop 的那段位移会被手势检测消耗，先单独跨过 slop 再拖主距离
            moveBy(Offset(viewConfiguration.touchSlop + 5f, 0f))
            moveBy(Offset(150f, 0f))
            up()
        }
        composeRule.waitUntil(5_000) { dotWidthPx() >= before + 150f - 2f }
    }

    @Test
    fun leftColumnDragRaisesBrightness() {
        composeRule.onNodeWithTag("homeRoot").performTouchInput {
            down(Offset(width / 6f, height * 0.5f))
            moveBy(Offset(0f, -height / 4f))
            up()
        }
        composeRule.waitForIdle()
        // 清空后未设置亮度，拖动基线为 0.5，上滑增加 1/4 屏高
        assertEquals(0.75f, composeRule.activity.window.attributes.screenBrightness, 0.05f)
    }

    @Test
    fun tapTopMiddleShowsHueRingAtHalfShortSide() {
        tapTopMiddle()
        composeRule.onNodeWithTag("hueRing").assertExists()
        val rootSize = rootBounds().size
        val ringSize = ringBounds().size
        assertEquals(min(rootSize.width, rootSize.height) / 2f, ringSize.width.toFloat(), 2f)
    }

    @Test
    fun indicatorSitsOnRingCenterline() {
        tapTopMiddle()
        composeRule.waitForIdle()
        val ring = ringBounds()
        val indicatorCenter = composeRule.onNodeWithTag("hueIndicator")
            .fetchSemanticsNode().boundsInRoot.center
        val strokePx = with(composeRule.density) { 24.dp.toPx() }
        val expectedOrbit = ring.size.width / 2f - strokePx / 2f
        val actualOrbit = hypot(
            indicatorCenter.x - ring.center.x,
            indicatorCenter.y - ring.center.y,
        )
        assertEquals(expectedOrbit, actualOrbit, 3f)
    }

    @Test
    fun ringDragMovesIndicatorToNewHue() {
        tapTopMiddle()
        val orbit = ringOrbitPx()
        composeRule.onNodeWithTag("hueRing").performTouchInput {
            down(Offset(center.x + orbit, center.y)) // 0° 红色
            moveTo(Offset(center.x, center.y + orbit)) // 90° 绿色
            up()
        }
        composeRule.waitForIdle()
        val rootCenter = rootBounds().center
        val indicatorCenter = composeRule.onNodeWithTag("hueIndicator")
            .fetchSemanticsNode().boundsInRoot.center
        assertEquals(orbit, indicatorCenter.y - rootCenter.y, 10f)
        assertEquals(rootCenter.x, indicatorCenter.x, 10f)
    }

    @Test
    fun tapTopMiddleAgainExitsAndRestoresDotSize() {
        // 进入取色模式前先记录原始尺寸，退出后圆点应恢复到该值
        val dotSizeBefore = dotWidthPx()
        tapTopMiddle()
        composeRule.onNodeWithTag("hueRing").assertExists()
        tapTopMiddle()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("hueRing").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("hueRing").assertDoesNotExist()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - dotSizeBefore) < 2f }
    }

    @Test
    fun hueIsPersistedOnExit() {
        tapTopMiddle()
        val orbit = ringOrbitPx()
        composeRule.onNodeWithTag("hueRing").performTouchInput {
            down(Offset(center.x + orbit, center.y))
            moveTo(Offset(center.x, center.y + orbit)) // 拖到 90°
            up()
        }
        tapTopMiddle() // 退出时持久化
        // DataStore 写入是异步的，轮询直到 hue 出现且接近 90°
        val hueKey = floatPreferencesKey("hue")
        composeRule.waitUntil(5_000) {
            val hue = runBlocking { targetContext.settingsDataStoreForTest.data.first() }[hueKey]
            hue != null && abs(hue - 90f) < 5f
        }
    }

    private fun tapTopMiddle() {
        composeRule.onNodeWithTag("homeRoot").performTouchInput {
            click(Offset(width / 2f, height / 6f))
        }
    }

    private fun rootBounds() = composeRule.onNodeWithTag("homeRoot")
        .fetchSemanticsNode().boundsInRoot

    private fun ringBounds() = composeRule.onNodeWithTag("hueRing")
        .fetchSemanticsNode().boundsInRoot

    private fun ringOrbitPx(): Float {
        val strokePx = with(composeRule.density) { 24.dp.toPx() }
        return ringBounds().size.width / 2f - strokePx / 2f
    }

    private fun dotWidthPx(): Float =
        composeRule.onNodeWithTag("dot").fetchSemanticsNode().boundsInRoot.width

    private fun minDotPx(): Float = with(composeRule.density) { 2.dp.toPx() }
}
