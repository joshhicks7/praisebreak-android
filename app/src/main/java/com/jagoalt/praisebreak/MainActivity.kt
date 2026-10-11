package com.jagoalt.praisebreak

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jagoalt.praisebreak.service.AppWatchService
import com.jagoalt.praisebreak.ui.PraiseApp
import com.jagoalt.praisebreak.ui.theme.PraiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val night = Color.parseColor("#0C1416")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(night),
            navigationBarStyle = SystemBarStyle.dark(night)
        )
        setContent {
            PraiseTheme {
                PraiseApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppWatchService.refreshWatch()
    }
}
