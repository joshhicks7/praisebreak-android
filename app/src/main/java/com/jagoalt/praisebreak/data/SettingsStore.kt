package com.jagoalt.praisebreak.data

import android.content.Context
import com.jagoalt.praisebreak.domain.model.BreakKind
import com.jagoalt.praisebreak.domain.model.CoverStyle
import com.jagoalt.praisebreak.domain.model.PraiseSettings
import com.jagoalt.praisebreak.domain.model.Weekday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val gate = Any()
    private val settingsState = MutableStateFlow(readSettings())
    private val runtimeState = MutableStateFlow(readRuntime())

    val settings: StateFlow<PraiseSettings> = settingsState.asStateFlow()
    val runtime: StateFlow<RuntimeState> = runtimeState.asStateFlow()

    fun update(block: (PraiseSettings) -> PraiseSettings) {
        synchronized(gate) {
            val next = block(settingsState.value)
            settingsState.value = next
            prefs.edit()
                .putBoolean(KEY_ENABLED, next.enabled)
                .putStringSet(KEY_PACKAGES, next.packages.toSet())
                .putInt(KEY_DAYS, next.days)
                .putInt(KEY_START, next.startMinute)
                .putInt(KEY_END, next.endMinute)
                .putInt(KEY_EVERY, next.everyMinutes)
                .putInt(KEY_BREAK, next.breakSeconds)
                .putString(KEY_STYLE, next.style.name)
                .putStringSet(KEY_KINDS, next.kinds.map { it.name }.toSet())
                .putInt(KEY_BIBLE_ID, next.bibleId)
                .putString(KEY_BIBLE_ABBR, next.bibleAbbreviation)
                .putString(KEY_BIBLE_TITLE, next.bibleTitle)
                .putString(KEY_BIBLE_CREDIT, next.bibleAttribution)
                .putString(KEY_API, next.apiKey)
                .apply()
        }
    }

    fun writeRuntime(next: RuntimeState) {
        synchronized(gate) {
            runtimeState.value = next
            prefs.edit()
                .putLong(KEY_ACCUM, next.accumulatedMs)
                .putLong(KEY_SEGMENT, next.segmentStartMs)
                .putLong(KEY_SNOOZE, next.snoozeUntilMs)
                .putInt(KEY_BREAKS, next.breaksToday)
                .putInt(KEY_DAY, next.breaksDay)
                .apply()
        }
    }

    private fun readSettings(): PraiseSettings {
        val kinds = prefs.getStringSet(KEY_KINDS, null)
            ?.mapNotNull { runCatching { BreakKind.valueOf(it) }.getOrNull() }
            ?.toSet()
            .orEmpty()
        val style = prefs.getString(KEY_STYLE, null)
            ?.let { runCatching { CoverStyle.valueOf(it) }.getOrNull() }
            ?: CoverStyle.Popup
        return PraiseSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            packages = prefs.getStringSet(KEY_PACKAGES, emptySet()).orEmpty().toSet(),
            days = prefs.getInt(KEY_DAYS, Weekday.all),
            startMinute = prefs.getInt(KEY_START, 8 * 60),
            endMinute = prefs.getInt(KEY_END, 22 * 60),
            everyMinutes = prefs.getInt(KEY_EVERY, 20),
            breakSeconds = prefs.getInt(KEY_BREAK, 60),
            style = style,
            kinds = kinds.ifEmpty { BreakKind.values().toSet() },
            bibleId = prefs.getInt(KEY_BIBLE_ID, 3034),
            bibleAbbreviation = prefs.getString(KEY_BIBLE_ABBR, "BSB").orEmpty(),
            bibleTitle = prefs.getString(KEY_BIBLE_TITLE, "Berean Standard Bible").orEmpty(),
            bibleAttribution = prefs.getString(KEY_BIBLE_CREDIT, "").orEmpty(),
            apiKey = prefs.getString(KEY_API, "").orEmpty()
        )
    }

    private fun readRuntime(): RuntimeState {
        return RuntimeState(
            accumulatedMs = prefs.getLong(KEY_ACCUM, 0),
            segmentStartMs = prefs.getLong(KEY_SEGMENT, 0),
            snoozeUntilMs = prefs.getLong(KEY_SNOOZE, 0),
            breaksToday = prefs.getInt(KEY_BREAKS, 0),
            breaksDay = prefs.getInt(KEY_DAY, 0)
        )
    }

    private companion object {
        const val FILE = "praise_break"
        const val KEY_ENABLED = "enabled"
        const val KEY_PACKAGES = "packages"
        const val KEY_DAYS = "days"
        const val KEY_START = "start"
        const val KEY_END = "end"
        const val KEY_EVERY = "every"
        const val KEY_BREAK = "break"
        const val KEY_STYLE = "style"
        const val KEY_KINDS = "kinds"
        const val KEY_BIBLE_ID = "bible_id"
        const val KEY_BIBLE_ABBR = "bible_abbr"
        const val KEY_BIBLE_TITLE = "bible_title"
        const val KEY_BIBLE_CREDIT = "bible_credit"
        const val KEY_API = "api_key"
        const val KEY_ACCUM = "accum"
        const val KEY_SEGMENT = "segment"
        const val KEY_SNOOZE = "snooze"
        const val KEY_BREAKS = "breaks"
        const val KEY_DAY = "day"
    }
}
