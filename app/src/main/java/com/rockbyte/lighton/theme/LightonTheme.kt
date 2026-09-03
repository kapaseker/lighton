package com.rockbyte.lighton.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.rockbyte.lighton.R

@Immutable
data class LightonColors(
    val ink: Color,
    val inkSubtle: Color,
    val inkMuted: Color,
    val line: Color,
    val lineStrong: Color,
    val canvas: Color,
    val mutedSurface: Color,
    val surface: Color,
)

@Immutable
data class LightonTypography(
    val display: TextStyle,
    val title: TextStyle,
    val section: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val data: TextStyle,
)

@Immutable
data class LightonShapes(
    val control: Shape,
    val surface: Shape,
    val image: Shape,
)

@Immutable
data class LightonDimensions(
    val touchTarget: Dp,
    val screenGutter: Dp,
    val relatedGap: Dp,
    val itemGap: Dp,
    val inlineLabelGap: Dp,
    val inlineIconTextGap: Dp,
    val workflowGap: Dp,
    val resultBlockPadding: Dp,
    val dialogPadding: Dp,
    val dialogContentPadding: Dp,
    val dialogGap: Dp,
    val appBarHorizontalPadding: Dp,
    val appBarIconSize: Dp,
    val inlineIconSize: Dp,
    val sampleSwatchSize: Dp,
    val graphHeight: Dp,
    val borderWidth: Dp,
    val dividerHeight: Dp,
    val controlCornerRadius: Dp,
    val surfaceCornerRadius: Dp,
    val imageCornerRadius: Dp,
)

@Composable
private fun defaultLightonColors() = LightonColors(
    ink = colorResource(R.color.lighton_ink),
    inkSubtle = colorResource(R.color.lighton_ink_subtle),
    inkMuted = colorResource(R.color.lighton_ink_muted),
    line = colorResource(R.color.lighton_line),
    lineStrong = colorResource(R.color.lighton_line_strong),
    canvas = colorResource(R.color.lighton_canvas),
    mutedSurface = colorResource(R.color.lighton_muted_surface),
    surface = colorResource(R.color.lighton_surface),
)

@Composable
private fun defaultLightonDimensions() = LightonDimensions(
    touchTarget = dimensionResource(R.dimen.lighton_touch_target),
    screenGutter = dimensionResource(R.dimen.lighton_screen_gutter),
    relatedGap = dimensionResource(R.dimen.lighton_related_gap),
    itemGap = dimensionResource(R.dimen.lighton_item_gap),
    inlineLabelGap = dimensionResource(R.dimen.lighton_inline_label_gap),
    inlineIconTextGap = dimensionResource(R.dimen.lighton_inline_icon_text_gap),
    workflowGap = dimensionResource(R.dimen.lighton_workflow_gap),
    resultBlockPadding = dimensionResource(R.dimen.lighton_result_block_padding),
    dialogPadding = dimensionResource(R.dimen.lighton_dialog_padding),
    dialogContentPadding = dimensionResource(R.dimen.lighton_dialog_content_padding),
    dialogGap = dimensionResource(R.dimen.lighton_dialog_gap),
    appBarHorizontalPadding = dimensionResource(R.dimen.lighton_app_bar_horizontal_padding),
    appBarIconSize = dimensionResource(R.dimen.lighton_app_bar_icon_size),
    inlineIconSize = dimensionResource(R.dimen.lighton_inline_icon_size),
    sampleSwatchSize = dimensionResource(R.dimen.lighton_sample_swatch_size),
    graphHeight = dimensionResource(R.dimen.lighton_graph_height),
    borderWidth = dimensionResource(R.dimen.lighton_border_width),
    dividerHeight = dimensionResource(R.dimen.lighton_divider_height),
    controlCornerRadius = dimensionResource(R.dimen.lighton_control_corner_radius),
    surfaceCornerRadius = dimensionResource(R.dimen.lighton_surface_corner_radius),
    imageCornerRadius = dimensionResource(R.dimen.lighton_image_corner_radius),
)

@Composable
private fun defaultLightonTypography() = LightonTypography(
    display = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_display_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_display_line_height),
        fontWeight = FontWeight.Bold,
    ),
    title = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_title_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_title_line_height),
        fontWeight = FontWeight.Bold,
    ),
    section = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_section_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_section_line_height),
        fontWeight = FontWeight.Bold,
    ),
    body = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_body_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_body_line_height),
    ),
    bodySmall = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_body_small_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_body_small_line_height),
    ),
    label = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_label_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_label_line_height),
        fontWeight = FontWeight.Bold,
    ),
    data = TextStyle(
        fontSize = textUnitResource(R.dimen.lighton_data_font_size),
        lineHeight = textUnitResource(R.dimen.lighton_data_line_height),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
    ),
)

private fun defaultLightonShapes(dimensions: LightonDimensions) = LightonShapes(
    control = RoundedCornerShape(dimensions.controlCornerRadius),
    surface = RoundedCornerShape(dimensions.surfaceCornerRadius),
    image = RoundedCornerShape(dimensions.imageCornerRadius),
)

@Composable
private fun textUnitResource(resourceId: Int): TextUnit = with(LocalDensity.current) {
    LocalResources.current.getDimension(resourceId).toSp()
}

internal val LocalLightonColors = staticCompositionLocalOf<LightonColors> {
    error("LightonTheme is not provided")
}
internal val LocalLightonTypography = staticCompositionLocalOf<LightonTypography> {
    error("LightonTheme is not provided")
}
internal val LocalLightonShapes = staticCompositionLocalOf<LightonShapes> {
    error("LightonTheme is not provided")
}
internal val LocalLightonDimensions = staticCompositionLocalOf<LightonDimensions> {
    error("LightonTheme is not provided")
}

object LightonTheme {
    val colors: LightonColors
        @Composable @ReadOnlyComposable get() = LocalLightonColors.current
    val typography: LightonTypography
        @Composable @ReadOnlyComposable get() = LocalLightonTypography.current
    val shapes: LightonShapes
        @Composable @ReadOnlyComposable get() = LocalLightonShapes.current
    val dimensions: LightonDimensions
        @Composable @ReadOnlyComposable get() = LocalLightonDimensions.current
    val styles: LightonComponentStyles
        get() = LightonComponentStyles
}

@Composable
fun LightonTheme(content: @Composable () -> Unit) {
    val colors = defaultLightonColors()
    val dimensions = defaultLightonDimensions()
    val typography = defaultLightonTypography()
    val shapes = defaultLightonShapes(dimensions)
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLightonColors provides colors,
        LocalLightonTypography provides typography,
        LocalLightonShapes provides shapes,
        LocalLightonDimensions provides dimensions,
        content = content,
    )
}

val Monospace = FontFamily.Monospace
