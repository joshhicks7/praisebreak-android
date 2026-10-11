package com.jagoalt.praisebreak.ui.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagoalt.praisebreak.domain.model.BibleVersion
import java.text.Collator
import com.jagoalt.praisebreak.ui.PraiseViewModel
import com.jagoalt.praisebreak.ui.VersionLoad

@Composable
fun BibleRoute(model: PraiseViewModel, onBack: () -> Unit) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val versions by model.versions.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { model.loadVersions() }
    BibleScreen(
        query = query,
        onQuery = { query = it },
        selectedId = settings.bibleId,
        versions = versions,
        onBack = onBack,
        onRetry = { model.loadVersions(force = true) },
        onSelect = model::selectBible
    )
}

@Composable
fun BibleScreen(
    query: String,
    onQuery: (String) -> Unit,
    selectedId: Int,
    versions: VersionLoad,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelect: (BibleVersion) -> Unit
) {
    val collator = remember { Collator.getInstance().apply { strength = Collator.PRIMARY } }
    val shown = (versions as? VersionLoad.Ready)?.items.orEmpty()
        .filter { version ->
            val needle = query.trim()
            needle.isEmpty() ||
                version.title.contains(needle, true) ||
                version.abbreviation.contains(needle, true) ||
                version.language.contains(needle, true)
        }
        .sortedWith { left, right ->
            val byTitle = collator.compare(left.title, right.title)
            if (byTitle != 0) byTitle else collator.compare(left.abbreviation, right.abbreviation)
        }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Bible version", style = MaterialTheme.typography.headlineMedium)
        }
        when (versions) {
            VersionLoad.Idle, VersionLoad.Loading -> item { CircularProgressIndicator() }
            is VersionLoad.Failed -> item {
                Text(versions.message, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("Try again") }
            }
            is VersionLoad.Ready -> {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQuery,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search versions") },
                        singleLine = true
                    )
                }
                items(shown, key = { it.id }) { version ->
                    VersionRow(version, version.id == selectedId) { onSelect(version) }
                }
            }
        }
    }
}

@Composable
private fun VersionRow(version: BibleVersion, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = if (selected) "${version.abbreviation} · selected" else version.abbreviation,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(version.title, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
