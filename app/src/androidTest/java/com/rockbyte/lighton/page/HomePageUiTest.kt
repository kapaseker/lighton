package com.rockbyte.lighton.page

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rockbyte.lighton.MainActivity
import com.rockbyte.lighton.store.settingsDataStoreForTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.min
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// 首页九宫格手势与取色模式的 UI 测试：走真实 App 栈（真实 DataStore），用例前后清空设置
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
    fun tapCenterExpandsDotToHalfShortSide() {
        tapCenter()
        val target = rootBounds().let { min(it.width, it.height) / 2f }
        // spring 收敛是渐近过程，收敛到容差内即认为到位
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - target) < 2f }
    }

    @Test
    fun slidersShownBelowDot() {
        tapCenter()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("sliderR", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val dotBottom = dotBounds().bottom
        listOf("sliderR", "sliderG", "sliderB").forEach { tag ->
            val bounds = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot
            assertTrue("$tag should be below dot", bounds.top >= dotBottom)
        }
        // 三条滑条从上到下排列
        val rTop = sliderTop("sliderR")
        val gTop = sliderTop("sliderG")
        val bTop = sliderTop("sliderB")
        assertTrue(rTop < gTop && gTop < bTop)
    }

    @Test
    fun sliderDragChangesDotColor() {
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        // 进入取色模式未拖动时为白色；把 R 滑条拖到最左，圆点应变青色（R 分量归零）
        val redBefore = dotCenterRedChannel()
        composeRule.onNodeWithTag("sliderR", useUnmergedTree = true).performTouchInput {
            down(Offset(center.x, center.y))
            moveTo(Offset(0f, center.y))
            up()
        }
        composeRule.waitForIdle()
        val redAfter = dotCenterRedChannel()
        assertTrue("red channel $redBefore -> $redAfter should decrease", redAfter < redBefore - 100)
    }

    @Test
    fun tapCenterAgainDoesNotExit() {
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        // 中间区域不再承担退出职责，取色模式应保持
        tapCenter()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("sliderR", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun cancelButtonExitsAndRestoresDotSize() {
        // 进入取色模式前先记录原始尺寸，取消退出后圆点应恢复到该值
        val dotSizeBefore = dotWidthPx()
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        tapCancelColorMode()
        // 滑条随取色模式一起移除，圆点 spring 收敛回原尺寸
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("sliderR", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - dotSizeBefore) < 2f }
    }

    @Test
    fun cancelButtonRestoresColorBeforeEditing() {
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        // 未设置颜色进入取色模式时圆点为白（红通道 255）
        val redBefore = dotCenterRedChannel()
        // 把 R 滑条拖到最左 → 圆点变青（红通道大幅下降）
        composeRule.onNodeWithTag("sliderR", useUnmergedTree = true).performTouchInput {
            down(Offset(center.x, center.y))
            moveTo(Offset(0f, center.y))
            up()
        }
        composeRule.waitForIdle()
        assertTrue("red channel should drop after slider drag", dotCenterRedChannel() < redBefore - 100)
        // 取消应恢复进入前颜色
        tapCancelColorMode()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("sliderR", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitUntil(5_000) { abs(dotCenterRedChannel() - redBefore) < 10 }
    }

    @Test
    fun eyeCareSwatchSelectChangesDotColorAndPersistsOnConfirm() {
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        // 护眼色板悬于圆点上方
        val dotTop = dotBounds().top
        val swatchBottom = composeRule.onNodeWithTag("eyeCareSwatch0", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.bottom
        assertTrue("eyeCareSwatch should be above dot", swatchBottom <= dotTop + 1f)
        composeRule.onNodeWithTag("eyeCareSwatch0", useUnmergedTree = true)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
        // 圆点立即变为绿豆沙 #C7EDCC（红通道 199）
        val red = dotCenterRedChannel()
        assertTrue("dot red channel should be ~199, was $red", abs(red - 199) < 10)
        // 选中下标在确认后持久化
        tapSaveColorMode()
        val indexKey = intPreferencesKey("eye_care_index")
        composeRule.waitUntil(5_000) {
            runBlocking { targetContext.settingsDataStoreForTest.data.first() }[indexKey] == 0
        }
    }

    @Test
    fun checkButtonPersistsColorOnExit() {
        tapCenter()
        composeRule.waitUntil(5_000) { abs(dotWidthPx() - colorTargetPx()) < 2f }
        // 把 R 滑条拖到最左 → color_r ≈ 0
        composeRule.onNodeWithTag("sliderR", useUnmergedTree = true).performTouchInput {
            down(Offset(center.x, center.y))
            moveTo(Offset(0f, center.y))
            up()
        }
        tapSaveColorMode() // 确认退出时持久化
        // DataStore 写入是异步的，轮询直到 color_r 出现且接近 0
        val redKey = floatPreferencesKey("color_r")
        composeRule.waitUntil(5_000) {
            val red = runBlocking { targetContext.settingsDataStoreForTest.data.first() }[redKey]
            red != null && red < 0.1f
        }
    }

    private fun tapCenter() {
        composeRule.onNodeWithTag("homeRoot").performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
    }

    private fun tapSaveColorMode() {
        composeRule.onNodeWithTag("saveColorMode").performTouchInput { click(center) }
    }

    private fun tapCancelColorMode() {
        composeRule.onNodeWithTag("cancelColorMode").performTouchInput { click(center) }
    }

    private fun rootBounds() = composeRule.onNodeWithTag("homeRoot")
        .fetchSemanticsNode().boundsInRoot

    private fun dotBounds() = composeRule.onNodeWithTag("dot")
        .fetchSemanticsNode().boundsInRoot

    private fun dotWidthPx(): Float = dotBounds().width

    private fun colorTargetPx() = rootBounds().let { min(it.width, it.height) / 2f }

    private fun sliderBounds(tag: String) = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
        .fetchSemanticsNode().boundsInRoot

    private fun sliderTop(tag: String) = sliderBounds(tag).top

    private fun dotCenterRedChannel(): Int {
        val root = rootBounds()
        val bmp = composeRule.onRoot().captureToImage().asAndroidBitmap()
        return android.graphics.Color.red(bmp.getPixel(root.center.x.toInt(), root.center.y.toInt()))
    }

    private fun minDotPx(): Float = with(composeRule.density) { 2.dp.toPx() }
}
