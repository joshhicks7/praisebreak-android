package com.jagoalt.praisebreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagoalt.praisebreak.BuildConfig
import com.jagoalt.praisebreak.data.bible.BibleLanguages
import com.jagoalt.praisebreak.data.apps.AppUse
import com.jagoalt.praisebreak.data.apps.UsageAccess
import com.jagoalt.praisebreak.domain.model.BibleVersion
import com.jagoalt.praisebreak.domain.model.BreakKind
import com.jagoalt.praisebreak.domain.model.PraiseSettings
import com.jagoalt.praisebreak.graph.AppGraph
import com.jagoalt.praisebreak.access.BreakAccess
import com.jagoalt.praisebreak.access.BreakAccessState
import com.jagoalt.praisebreak.service.AppWatchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VersionLoad {
    data object Idle : VersionLoad
    data object Loading : VersionLoad
    data class Ready(val items: List<BibleVersion>) : VersionLoad
    data class Failed(val message: String) : VersionLoad
}

class PraiseViewModel(app: Application) : AndroidViewModel(app) {
    private val graph = AppGraph.of(app)
    val settings: StateFlow<PraiseSettings> = graph.settings.settings
    val runtime = graph.settings.runtime

    private val accessState = MutableStateFlow(BreakAccess.read(app))
    val access: StateFlow<BreakAccessState> = accessState.asStateFlow()

    private val usageState = MutableStateFlow<List<AppUse>>(emptyList())
    val usage: StateFlow<List<AppUse>> = usageState.asStateFlow()

    private val versionState = MutableStateFlow<VersionLoad>(VersionLoad.Idle)
    val versions: StateFlow<VersionLoad> = versionState.asStateFlow()

    fun update(block: (PraiseSettings) -> PraiseSettings) {
        graph.settings.update(block)
    }

    fun refreshAccess() {
        val context = getApplication<Application>()
        accessState.value = BreakAccess.read(context)
        AppWatchService.refreshWatch()
        viewModelScope.launch(Dispatchers.IO) {
            usageState.value = UsageAccess.today(context)
        }
    }

    fun togglePackage(packageName: String, on: Boolean) {
        update { current ->
            val next = if (on) current.packages + packageName else current.packages - packageName
            current.copy(packages = next)
        }
    }

    fun setWatched(packages: Set<String>) {
        update { it.copy(packages = packages) }
    }

    fun toggleKind(kind: BreakKind, on: Boolean) {
        update { current ->
            val next = if (on) current.kinds + kind else current.kinds - kind
            if (next.isEmpty()) current else current.copy(kinds = next)
        }
    }

    fun selectBible(version: BibleVersion) {
        update {
            it.copy(
                bibleId = version.id,
                bibleAbbreviation = version.abbreviation,
                bibleTitle = version.title,
                bibleAttribution = version.copyright
            )
        }
    }

    fun loadVersions(force: Boolean = false) {
        if (!force && versionState.value is VersionLoad.Ready) return
        val key = BuildConfig.YVP_APP_KEY.trim()
        if (key.isBlank()) {
            versionState.value = VersionLoad.Failed("Bible versions aren't available right now.")
            return
        }
        val language = BibleLanguages.code()
        viewModelScope.launch {
            if (!force) {
                val cached = graph.versionCache.read(language)
                if (cached != null) {
                    versionState.value = VersionLoad.Ready(cached)
                    return@launch
                }
            }
            versionState.value = VersionLoad.Loading
            versionState.value = runCatching { graph.bibles.versions(key, language) }
                .fold(
                    onSuccess = {
                        graph.versionCache.write(language, it)
                        VersionLoad.Ready(it)
                    },
                    onFailure = { error ->
                        val cached = graph.versionCache.read(language)
                        if (cached != null) VersionLoad.Ready(cached)
                        else VersionLoad.Failed(error.message ?: "Couldn't load Bible versions.")
                    }
                )
        }
    }
}
