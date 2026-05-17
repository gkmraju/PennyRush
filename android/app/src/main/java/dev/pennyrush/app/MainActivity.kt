package dev.pennyrush.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.pennyrush.core.designsystem.PennyrushTheme
import dev.pennyrush.feature.home.HomeRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PennyrushTheme {
                HomeRoute()
            }
        }
    }
}
