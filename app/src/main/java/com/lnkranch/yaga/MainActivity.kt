package com.lnkranch.yaga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lnkranch.yaga.ui.navigation.AppNavigation
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DrillApplication
        setContent {
            ChordToneDrillTheme {
                AppNavigation(app)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ChordToneDrillTheme {
        // AppNavigation requires an application instance, 
        // but we can at least preview the theme's default state
        // or a specific screen here.
    }
}
