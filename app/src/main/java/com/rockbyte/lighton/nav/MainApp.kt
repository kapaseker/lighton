package com.rockbyte.lighton.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.rockbyte.lighton.page.HomePage
import com.rockbyte.lighton.theme.LightonTheme

@Composable
fun MainApp() {
    val backStack = remember { mutableStateListOf<Any>(HomeNav) }
    val pageStyleState = remember { MutableStyleState(null) }
    val onBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize().styleable(pageStyleState, LightonTheme.styles.page),
        backStack = backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is HomeNav -> NavEntry(key) {
                    HomePage()
                }

                else -> error("Unsupported navigation key: $key")
            }
        },
    )
}
