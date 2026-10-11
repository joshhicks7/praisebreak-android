package com.jagoalt.praisebreak.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagoalt.praisebreak.access.BreakAccess
import com.jagoalt.praisebreak.ui.PraiseViewModel
import com.jagoalt.praisebreak.ui.components.QuietCard
import com.jagoalt.praisebreak.ui.home.AccessNeed

@Composable
fun PermissionsRoute(model: PraiseViewModel, onBack: () -> Unit) {
    val access by model.access.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Permissions", style = MaterialTheme.typography.headlineMedium)
        QuietCard {
            AccessNeed(
                "Display over other apps",
                "Breaks show on top of other apps.",
                access.cover
            ) { context.startActivity(BreakAccess.coverSettings(context)) }
            AccessNeed(
                "Accessibility",
                "Sees which app is open, not what is on screen.",
                access.whichApp
            ) { context.startActivity(BreakAccess.whichAppSettings()) }
            AccessNeed(
                "Usage access",
                "Today's time in each app.",
                access.today
            ) { context.startActivity(BreakAccess.todaySettings()) }
        }
    }
}
