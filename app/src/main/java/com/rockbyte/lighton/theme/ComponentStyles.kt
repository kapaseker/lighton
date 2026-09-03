package com.rockbyte.lighton.theme

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.size

val StyleScope.lightonColors: LightonColors
    get() = LocalLightonColors.currentValue

val StyleScope.lightonTypography: LightonTypography
    get() = LocalLightonTypography.currentValue

val StyleScope.lightonShapes: LightonShapes
    get() = LocalLightonShapes.currentValue

val StyleScope.lightonDimensions: LightonDimensions
    get() = LocalLightonDimensions.currentValue

object LightonComponentStyles {
    val page = Style {
        background(lightonColors.canvas)
        contentColor(lightonColors.ink)
    }

    val appBar = Style {
        background(lightonColors.surface)
        contentColor(lightonColors.ink)
    }

    val appBarContent = Style {
        height(lightonDimensions.touchTarget)
        contentPaddingHorizontal(lightonDimensions.appBarHorizontalPadding)
    }

    val appBarIcon = Style { size(lightonDimensions.appBarIconSize) }

    val outlinedSurface = Style {
        background(lightonColors.surface)
        borderColor(lightonColors.line)
        borderWidth(lightonDimensions.borderWidth)
        shape(lightonShapes.control)
        contentColor(lightonColors.ink)
    }

    val imageSurface = Style {
        background(lightonColors.mutedSurface)
        borderColor(lightonColors.line)
        borderWidth(lightonDimensions.borderWidth)
        shape(lightonShapes.image)
    }

    val resultSurface = Style {
        background(lightonColors.ink)
        shape(lightonShapes.surface)
        contentColor(lightonColors.surface)
    }

    val primaryButton = Style {
        background(lightonColors.ink)
        shape(lightonShapes.control)
        minHeight(lightonDimensions.touchTarget)
        contentPaddingHorizontal(lightonDimensions.screenGutter)
        contentColor(lightonColors.surface)
        pressed { alpha(.8f) }
        disabled {
            background(lightonColors.mutedSurface)
            contentColor(lightonColors.inkMuted)
        }
    }

    val outlinedButton = Style {
        background(lightonColors.surface)
        borderColor(lightonColors.ink)
        borderWidth(lightonDimensions.borderWidth)
        shape(lightonShapes.control)
        minHeight(lightonDimensions.touchTarget)
        contentPaddingHorizontal(lightonDimensions.screenGutter)
        contentColor(lightonColors.ink)
        pressed { alpha(.8f) }
        disabled {
            borderColor(lightonColors.line)
            contentColor(lightonColors.inkMuted)
        }
    }

    val textButton = Style {
        minHeight(lightonDimensions.touchTarget)
        contentPaddingHorizontal(lightonDimensions.relatedGap)
        contentColor(lightonColors.ink)
        pressed { alpha(.6f) }
    }

    val iconButton = Style {
        size(lightonDimensions.touchTarget)
        shape(lightonShapes.control)
        contentColor(lightonColors.ink)
        pressed { contentColor(lightonColors.inkMuted) }
    }

    val floatingActionButton = Style {
        size(lightonDimensions.touchTarget)
        background(lightonColors.ink)
        shape(lightonShapes.control)
        contentColor(lightonColors.surface)
        pressed { alpha(.8f) }
    }

    val textField = Style {
        background(lightonColors.surface)
        borderColor(lightonColors.lineStrong)
        borderWidth(lightonDimensions.borderWidth)
        shape(lightonShapes.control)
        minHeight(lightonDimensions.touchTarget)
        contentPadding(lightonDimensions.relatedGap)
        contentColor(lightonColors.ink)
        focused { borderColor(lightonColors.ink) }
        disabled {
            background(lightonColors.mutedSurface)
            contentColor(lightonColors.inkMuted)
        }
    }

    val divider = Style {
        background(lightonColors.line)
        height(lightonDimensions.dividerHeight)
    }

    val displayText = Style { textStyle(lightonTypography.display) }
    val titleText = Style { textStyle(lightonTypography.title) }
    val sectionText = Style { textStyle(lightonTypography.section) }
    val bodyText = Style { textStyle(lightonTypography.body) }
    val bodySmallText = Style { textStyle(lightonTypography.bodySmall) }
    val labelText = Style { textStyle(lightonTypography.label) }
    val dataText = Style { textStyle(lightonTypography.data) }
}
