package com.rockbyte.lighton.page

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.rockbyte.lighton.store.settingsDataStoreForTest
import kotlin.math.hypot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val minDotPx by lazy { with(rule.density) { 2.dp.toPx() } }

    @Before
    fun setUp() {
        // 清空持久化数据，避免测试间互相影响
        runBlocking {
            rule.activity.applicationContext.settingsDataStoreForTest.edit { it.clear() }
        }
        rule.setContent { HomePage() }
        setBrightness(0.5f)
    }

    @After
    fun tearDown() {
        // -1：恢复跟随系统亮度
        setBrightness(-1f)
    }

    private fun setBrightness(value: Float) {
        rule.runOnUiThread {
            val window = rule.activity.window
            val params = window.attributes
            params.screenBrightness = value
            window.attributes = params
        }
    }

    private fun brightness() = rule.activity.window.attributes.screenBrightness

    private fun dotWidthPx() = rule.onNodeWithTag("dot").fetchSemanticsNode().size.width

    private fun screenSize() = rule.onRoot().fetchSemanticsNode().size

    @Test
    fun initialDotIs2dpWhite() {
        rule.onNodeWithTag("dot")
            .assertWidthIsEqualTo(2.dp)
            .assertHeightIsEqualTo(2.dp)
    }

    @Test
    fun bottomRowHorizontalDrag_resizesDot() {
        val size = screenSize()
        val y = size.height * 5f / 6f
        val dx = size.width * 0.3f
        var slop = 0f

        rule.onRoot().performTouchInput {
            slop = viewConfiguration.touchSlop
            swipe(Offset(size.width * 0.5f, y), Offset(size.width * 0.5f + dx, y), 300)
        }

        // detectDragGestures 会先吃掉 touchSlop，所以实际增量在 (dx - slop, dx] 之间
        val width = dotWidthPx().toFloat()
        assertTrue(
            "dot width $width should be in (${minDotPx + dx - slop}, ${minDotPx + dx}]",
            width > minDotPx + dx - slop - 1f && width <= minDotPx + dx + 1f,
        )
    }

    @Test
    fun bottomLeftCorner_dragDirectionDecidesMode() {
        val size = screenSize()
        val x = size.width / 6f
        val y = size.height * 5f / 6f

        // 角落横向滑 → 调圆点，亮度不变
        rule.onRoot().performTouchInput {
            swipe(Offset(x, y), Offset(x + size.width * 0.3f, y), 300)
        }
        assertEquals(0.5f, brightness(), 0.001f)
        assertTrue("corner horizontal swipe should grow dot", dotWidthPx() > minDotPx)

        // 角落竖向滑 → 调亮度
        rule.onRoot().performTouchInput {
            swipe(Offset(x, y), Offset(x, y - size.height * 0.2f), 300)
        }
        assertTrue("corner vertical swipe should brighten", brightness() > 0.5f)
    }

    @Test
    fun dotSize_clampedBetweenMinAndScreenDiagonal() {
        val size = screenSize()
        val y = size.height * 5f / 6f

        // 持续向右滑，圆点最大铺满屏幕（对角线长度）
        repeat(10) {
            rule.onRoot().performTouchInput {
                swipe(Offset(size.width * 0.4f, y), Offset(size.width * 0.95f, y), 100)
            }
        }
        val maxPx = hypot(size.width.toFloat(), size.height.toFloat())
        assertEquals(
            "root=${size.width}x${size.height}, dot=${dotWidthPx()}",
            maxPx, dotWidthPx().toFloat(), 2f,
        )

        // 持续向左滑，圆点最小回到 2dp
        repeat(20) {
            rule.onRoot().performTouchInput {
                swipe(Offset(size.width * 0.95f, y), Offset(size.width * 0.4f, y), 100)
            }
        }
        rule.onNodeWithTag("dot").assertWidthIsEqualTo(2.dp)
    }

    @Test
    fun leftColumnVerticalDrag_adjustsBrightness() {
        val size = screenSize()
        val x = size.width / 6f

        rule.onRoot().performTouchInput {
            swipe(Offset(x, size.height * 0.6f), Offset(x, size.height * 0.5f), 300)
        }
        val afterUp = brightness()
        assertTrue("up swipe should brighten, was $afterUp", afterUp > 0.5f && afterUp <= 0.6f)

        rule.onRoot().performTouchInput {
            swipe(Offset(x, size.height * 0.4f), Offset(x, size.height * 0.6f), 300)
        }
        val afterDown = brightness()
        assertTrue(
            "down swipe should dim: $afterUp -> $afterDown",
            afterDown < afterUp && afterDown > afterUp - 0.21f,
        )

        // 亮度夹紧在 [0, 1]
        repeat(10) {
            rule.onRoot().performTouchInput {
                swipe(Offset(x, size.height * 0.3f), Offset(x, size.height * 0.7f), 100)
            }
        }
        assertEquals(0f, brightness(), 0.001f)

        repeat(10) {
            rule.onRoot().performTouchInput {
                swipe(Offset(x, size.height * 0.7f), Offset(x, size.height * 0.3f), 100)
            }
        }
        assertEquals(1f, brightness(), 0.001f)
    }

    @Test
    fun centerDrag_changesNothing() {
        val size = screenSize()

        rule.onRoot().performTouchInput {
            swipe(
                Offset(size.width * 0.5f, size.height * 0.5f),
                Offset(size.width * 0.6f, size.height * 0.4f),
                300,
            )
        }

        assertEquals(0.5f, brightness(), 0.001f)
        rule.onNodeWithTag("dot").assertWidthIsEqualTo(2.dp)
    }
}
