package com.jagoalt.praisebreak.ui.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagoalt.praisebreak.access.BreakAccessState
import com.jagoalt.praisebreak.domain.model.BreakKind
import com.jagoalt.praisebreak.domain.model.PraiseSettings
import com.jagoalt.praisebreak.ui.PraiseViewModel
import com.jagoalt.praisebreak.ui.components.ChoiceRow
import com.jagoalt.praisebreak.ui.components.QuietCard
import com.jagoalt.praisebreak.ui.components.ToggleLine

private val intervals = listOf(10, 15, 20, 30, 45, 60)
private val lengths = listOf(30, 60, 120, 300, 600)

@Composable
fun SetupRoute(model: PraiseViewModel, onBible: () -> Unit, onPermissions: () -> Unit) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val access by model.access.collectAsStateWithLifecycle()
    SetupScreen(
        settings = settings,
        access = access,
        onEvery = { index -> model.update { it.copy(everyMinutes = intervals[index]) } },
        onLength = { index -> model.update { it.copy(breakSeconds = lengths[index]) } },
        onKind = model::toggleKind,
        onBible = onBible,
        onPermissions = onPermissions,
        onBreaks = { model.update { current -> current.copy(enabled = it) } }
    )
}

@Composable
fun SetupScreen(
    settings: PraiseSettings,
    onEvery: (Int) -> Unit,
    onLength: (Int) -> Unit,
    onKind: (BreakKind, Boolean) -> Unit,
    onBible: () -> Unit,
    access: BreakAccessState,
    onPermissions: () -> Unit,
    onBreaks: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Setup", style = MaterialTheme.typography.headlineMedium)
        QuietCard {
            ToggleLine(
                title = "Breaks",
                body = if (settings.packages.isEmpty()) "Pick apps" else "",
                checked = settings.enabled,
                onChecked = onBreaks
            )
        }
        QuietCard(modifier = Modifier.clickable(onClick = onPermissions)) {
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            Text(
                permissionLine(access),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        QuietCard {
            Text("How often", style = MaterialTheme.typography.titleMedium)
            ChoiceRow(
                labels = intervals.map { "$it min" },
                isSelected = { index -> intervals[index] == settings.everyMinutes },
                onSelect = onEvery
            )
        }
        QuietCard {
            Text("How long", style = MaterialTheme.typography.titleMedium)
            ChoiceRow(
                labels = lengths.map { lengthLabel(it) },
                isSelected = { index -> lengths[index] == settings.breakSeconds },
                onSelect = onLength
            )
        }
        QuietCard {
            BreakKind.values().forEach { kind ->
                ToggleLine(
                    title = kind.title,
                    body = "",
                    checked = kind in settings.kinds,
                    onChecked = { onKind(kind, it) }
                )
            }
        }
        QuietCard(modifier = Modifier.clickable(onClick = onBible)) {
            Text("Bible", style = MaterialTheme.typography.titleMedium)
            Text(
                settings.bibleAbbreviation,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun permissionLine(access: BreakAccessState): String {
    val on = listOf(access.cover, access.whichApp, access.today).count { it }
    return if (on == 3) "On" else "$on of 3"
}

private fun lengthLabel(seconds: Int): String {
    return if (seconds < 60) "${seconds}s" else "${seconds / 60} min"
}
