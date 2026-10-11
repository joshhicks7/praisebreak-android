package com.jagoalt.praisebreak.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagoalt.praisebreak.data.apps.InstalledApps
import com.jagoalt.praisebreak.data.apps.LaunchableApp
import com.jagoalt.praisebreak.ui.PraiseViewModel
import com.jagoalt.praisebreak.ui.components.ToggleLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppsRoute(model: PraiseViewModel) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<LaunchableApp>>(emptyList()) }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { InstalledApps.load(context) }
    }
    AppsScreen(
        apps = apps,
        watched = settings.packages,
        onToggle = model::togglePackage,
        onSelectAll = { model.setWatched(apps.map { it.packageName }.toSet()) },
        onClearAll = { model.setWatched(emptySet()) }
    )
}

@Composable
fun AppsScreen(
    apps: List<LaunchableApp>,
    watched: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val shown = apps.filter { app ->
        val needle = query.trim()
        needle.isEmpty() ||
            app.label.contains(needle, ignoreCase = true) ||
            app.packageName.contains(needle, ignoreCase = true)
    }
    val allSelected = apps.isNotEmpty() && apps.all { it.packageName in watched }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Apps", style = MaterialTheme.typography.headlineMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${watched.count { pkg -> apps.any { it.packageName == pkg } }} of ${apps.size} counting",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = if (allSelected) onClearAll else onSelectAll, enabled = apps.isNotEmpty()) {
                        Text(if (allSelected) "Clear all" else "Select all")
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search") },
                    singleLine = true
                )
            }
        }
        items(shown, key = { it.packageName }) { app ->
            ToggleLine(
                title = app.label,
                body = "",
                checked = app.packageName in watched,
                onChecked = { onToggle(app.packageName, it) },
                leading = { AppIcon(app.packageName, app.label) }
            )
        }
    }
}
