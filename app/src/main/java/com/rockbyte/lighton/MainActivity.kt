package com.rockbyte.lighton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rockbyte.lighton.nav.MainApp
import com.rockbyte.lighton.theme.LightonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LightonTheme {
                MainApp()
            }
        }
    }
}
