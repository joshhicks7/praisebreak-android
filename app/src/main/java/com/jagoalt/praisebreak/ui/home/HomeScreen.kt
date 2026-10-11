package com.jagoalt.praisebreak.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagoalt.praisebreak.data.apps.AppUse
import com.jagoalt.praisebreak.ui.PraiseViewModel
import com.jagoalt.praisebreak.ui.components.QuietCard
import com.jagoalt.praisebreak.ui.timing.MostUsed
import com.jagoalt.praisebreak.ui.timing.durationLabel

@Composable
fun HomeRoute(model: PraiseViewModel, onAbout: () -> Unit) {
    val usage by model.usage.collectAsStateWithLifecycle()
    HomeScreen(
        minutesToday = usage.sumOf { it.minutes },
        usage = usage,
        onAbout = onAbout
    )
}

@Composable
fun HomeScreen(
    minutesToday: Int,
    usage: List<AppUse>,
    onAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Praise Break", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onAbout) { Text("?") }
        }
        QuietCard {
            Text(durationLabel(minutesToday), style = MaterialTheme.typography.headlineMedium)
        }
        MostUsed(usage)
    }
}
