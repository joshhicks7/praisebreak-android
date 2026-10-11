package com.jagoalt.praisebreak.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.jagoalt.praisebreak.BuildConfig
import com.jagoalt.praisebreak.data.RuntimeState
import com.jagoalt.praisebreak.data.SettingsStore
import com.jagoalt.praisebreak.data.bible.BibleRepository
import com.jagoalt.praisebreak.data.bible.PassageCatalog
import com.jagoalt.praisebreak.domain.BreakGate
import com.jagoalt.praisebreak.domain.GateDecision
import com.jagoalt.praisebreak.domain.Schedule
import com.jagoalt.praisebreak.domain.UsageClock
import com.jagoalt.praisebreak.domain.WatchSlice
import com.jagoalt.praisebreak.domain.model.BreakKind
import com.jagoalt.praisebreak.domain.model.CoverStyle
import com.jagoalt.praisebreak.domain.model.PraiseSettings
import com.jagoalt.praisebreak.overlay.BreakWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BreakDirector(
    private val context: Context,
    private val store: SettingsStore,
    private val bibles: BibleRepository,
    private val window: BreakWindow,
    private val scope: CoroutineScope
) {
    private val clock = UsageClock()
    private val handler = Handler(Looper.getMainLooper())
    private var tick: Runnable? = null
    private var foregroundPkg: String? = null
    private var foregroundLabel: String = ""
    private var inPraiseBreak = false
    private var open: OpenBreak? = null

    init {
        val runtime = store.runtime.value
        clock.restore(runtime.accumulatedMs, runtime.segmentStartMs)
        window.onContinue = { finish() }
        window.onSnooze = { snooze() }
    }

    fun start() {
        refresh()
    }

    fun stop() {
        tick?.let { handler.removeCallbacks(it) }
        window.detach()
    }

    fun onForeground(pkg: String?, label: String, insideApp: Boolean) {
        foregroundPkg = pkg
        foregroundLabel = label
        inPraiseBreak = insideApp
        refresh()
    }

    fun preview(): Boolean {
        if (!Settings.canDrawOverlays(context)) return false
        if (open != null) return true
        begin(store.settings.value, forced = true)
        return open != null
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        val settings = store.settings.value
        val current = open
        if (current != null && !settings.enabled && !current.forced) {
            clearOpen(now, settings)
            return
        }
        if (current != null) {
            val show = settings.watches(foregroundPkg) || (current.forced && inPraiseBreak)
            if (show) {
                window.render(current.face(labelFor()), now)
            } else {
                window.hide()
            }
            schedule(if (show) 1_000 else 5_000)
            return
        }
        val watched = tracking(settings)
        clock.setTracking(watched, now)
        persistClock(now)
        val decision = BreakGate.decide(now, settings, slice(now, watched))
        if (decision is GateDecision.Open) {
            begin(settings, forced = false)
            return
        }
        schedule(if (watched) 5_000 else 20_000)
    }

    private fun begin(settings: PraiseSettings, forced: Boolean) {
        val now = System.currentTimeMillis()
        clock.setTracking(false, now)
        val kind = settings.kinds.randomOrNull() ?: BreakKind.Thanks
        val passage = if (kind == BreakKind.Scripture) PassageCatalog.short.random() else null
        val key = BuildConfig.YVP_APP_KEY.trim()
        val duration = settings.breakSeconds.coerceIn(15, 30 * 60) * 1_000L
        val created = OpenBreak(
            token = now,
            kind = kind,
            style = CoverStyle.Freeze,
            endsAt = now + duration,
            durationMs = duration,
            body = "",
            reference = passage?.reference.orEmpty(),
            loading = passage != null && key.isNotBlank(),
            forced = forced
        )
        open = created
        persistClock(now)
        refresh()
        if (passage != null) loadVerse(created, settings, passage.id, key)
    }

    private fun loadVerse(created: OpenBreak, settings: PraiseSettings, passageId: String, key: String) {
        if (key.isBlank()) {
            replace(created.token) { it.copy(loading = false) }
            return
        }
        scope.launch {
            val result = runCatching { bibles.passage(key, settings.bibleId, passageId) }
            withContext(Dispatchers.Main) {
                replace(created.token) { current ->
                    val verse = result.getOrNull()
                    if (verse != null && verse.content.isNotBlank()) {
                        current.copy(
                            body = verse.content,
                            reference = verse.reference.ifBlank { current.reference },
                            loading = false
                        )
                    } else {
                        current.copy(loading = false)
                    }
                }
            }
        }
    }

    private fun finish() {
        val current = open ?: return
        val now = System.currentTimeMillis()
        if (now < current.endsAt) return
        val day = Schedule.dayKey(now)
        val runtime = store.runtime.value
        val breaks = if (runtime.breaksDay == day) runtime.breaksToday + 1 else 1
        open = null
        window.hide()
        val settings = store.settings.value
        clock.reset(stillTracking = false, nowMs = now)
        clock.setTracking(tracking(settings), now)
        store.writeRuntime(
            RuntimeState(
                accumulatedMs = clock.accumulatedMs,
                segmentStartMs = clock.segmentStartMs,
                snoozeUntilMs = runtime.snoozeUntilMs,
                breaksToday = breaks,
                breaksDay = day
            )
        )
        schedule(5_000)
    }

    private fun snooze() {
        val current = open ?: return
        if (current.style != com.jagoalt.praisebreak.domain.model.CoverStyle.Popup) return
        val now = System.currentTimeMillis()
        val settings = store.settings.value
        val minutes = (settings.everyMinutes / 2).coerceAtLeast(5)
        open = null
        window.hide()
        clock.reset(stillTracking = false, nowMs = now)
        clock.setTracking(tracking(settings), now)
        val runtime = store.runtime.value
        store.writeRuntime(
            runtime.copy(
                accumulatedMs = clock.accumulatedMs,
                segmentStartMs = clock.segmentStartMs,
                snoozeUntilMs = now + minutes * 60_000L,
                breaksDay = Schedule.dayKey(now)
            )
        )
        schedule(5_000)
    }

    private fun clearOpen(now: Long, settings: PraiseSettings) {
        open = null
        window.hide()
        clock.setTracking(tracking(settings), now)
        persistClock(now)
        schedule(20_000)
    }

    private fun replace(token: Long, block: (OpenBreak) -> OpenBreak) {
        val current = open ?: return
        if (current.token != token) return
        open = block(current)
        refresh()
    }

    private fun tracking(settings: PraiseSettings): Boolean {
        return settings.enabled && settings.watches(foregroundPkg)
    }

    private fun slice(now: Long, watched: Boolean): WatchSlice {
        return WatchSlice(
            watchedNow = watched,
            accumulatedMs = clock.elapsed(now),
            snoozeUntilMs = store.runtime.value.snoozeUntilMs,
            breakOpen = false
        )
    }

    private fun persistClock(now: Long) {
        val runtime = store.runtime.value
        val day = Schedule.dayKey(now)
        store.writeRuntime(
            runtime.copy(
                accumulatedMs = clock.accumulatedMs,
                segmentStartMs = clock.segmentStartMs,
                breaksToday = if (runtime.breaksDay == day) runtime.breaksToday else 0,
                breaksDay = day
            )
        )
    }

    private fun labelFor(): String {
        if (inPraiseBreak || foregroundLabel.equals("Praise Break", ignoreCase = true)) return ""
        return foregroundLabel
    }

    private fun schedule(delayMs: Long) {
        tick?.let { handler.removeCallbacks(it) }
        val job = Runnable { refresh() }
        tick = job
        handler.postDelayed(job, delayMs)
    }
}
