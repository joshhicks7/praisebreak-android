package com.jagoalt.praisebreak.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import com.jagoalt.praisebreak.graph.AppGraph
import com.jagoalt.praisebreak.overlay.BreakWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Notices which app is open so a break can cover it. Does not read screen content. */
class AppWatchService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pending: Runnable? = null
    private lateinit var director: BreakDirector
    private var homePackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!this::director.isInitialized) {
            val graph = AppGraph.of(this)
            director = BreakDirector(
                context = this,
                store = graph.settings,
                bibles = graph.bibles,
                window = BreakWindow(this),
                scope = scope
            )
            homePackages = loadHomePackages()
        }
        instance = this
        director.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val type = event.eventType
        val watched = type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            type == AccessibilityEvent.TYPE_WINDOWS_CHANGED
        if (!watched) return
        val pkg = event.packageName?.toString()
        val className = event.className?.toString().orEmpty()
        if (pkg == packageName && !className.endsWith("MainActivity")) return
        scheduleResolve(pkg, className)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        pending?.let { handler.removeCallbacks(it) }
        if (this::director.isInitialized) director.stop()
        scope.cancel()
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun scheduleResolve(eventPkg: String?, eventClass: String) {
        pending?.let { handler.removeCallbacks(it) }
        val job = Runnable { resolveForeground(eventPkg, eventClass) }
        pending = job
        handler.postDelayed(job, 220)
    }

    private fun resolveForeground(eventPkg: String?, eventClass: String) {
        if (!this::director.isInitialized) return
        val focused = focusedApp()
        val pkg = focused?.pkg ?: eventPkg
        val className = focused?.className ?: eventClass
        if (pkg.isNullOrBlank() || isTransient(pkg)) return
        if (pkg == packageName) {
            director.onForeground(null, "", insideApp = className.endsWith("MainActivity"))
            return
        }
        if (pkg in homePackages) {
            director.onForeground(null, "", insideApp = false)
            return
        }
        val label = resolveLabel(pkg)
        Log.i(TAG, "Foreground $label")
        director.onForeground(pkg, label, insideApp = false)
    }

    private fun focusedApp(): Foreground? {
        val listed = runCatching { windows }.getOrNull() ?: return null
        if (listed.isEmpty()) return null
        return try {
            val picks = listed.mapNotNull { window ->
                if (window.type != AccessibilityWindowInfo.TYPE_APPLICATION) return@mapNotNull null
                val root = window.root ?: return@mapNotNull null
                val pkg = root.packageName?.toString()
                val className = root.className?.toString().orEmpty()
                root.recycle()
                if (pkg.isNullOrBlank()) return@mapNotNull null
                if (pkg == packageName && !className.endsWith("MainActivity")) return@mapNotNull null
                WindowPick(pkg, className, window.isFocused, window.isActive, window.layer)
            }
            val pick = picks.firstOrNull { it.focused }
                ?: picks.filter { it.active }.maxByOrNull { it.layer }
                ?: return null
            Foreground(pick.pkg, pick.className)
        } finally {
            listed.forEach { runCatching { it.recycle() } }
        }
    }

    private fun isTransient(pkg: String): Boolean {
        if (pkg in TRANSIENT) return true
        val imm = getSystemService(InputMethodManager::class.java) ?: return false
        return imm.enabledInputMethodList.any { it.packageName == pkg }
    }

    private fun resolveLabel(pkg: String): String {
        return runCatching {
            val info = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(pkg)
    }

    private fun loadHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }.map { it.activityInfo.packageName }
        }.getOrDefault(emptyList())
        return resolved.toSet() + KNOWN_HOME
    }

    companion object {
        private const val TAG = "PraiseBreak"
        private val TRANSIENT = setOf(
            "android",
            "com.android.systemui",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.vpndialogs",
            "com.android.quickstep",
            "com.android.intentresolver",
            "com.google.android.documentsui"
        )
        private val KNOWN_HOME = setOf(
            "com.android.launcher",
            "com.android.launcher2",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.huawei.android.launcher",
            "com.oppo.launcher",
            "com.nothing.launcher",
            "com.microsoft.launcher"
        )

        @Volatile
        private var instance: AppWatchService? = null

        fun preview(): Boolean = instance?.director?.preview() == true

        fun refreshWatch() {
            instance?.director?.refresh()
        }
    }

    private data class Foreground(val pkg: String, val className: String)

    private data class WindowPick(
        val pkg: String,
        val className: String,
        val focused: Boolean,
        val active: Boolean,
        val layer: Int
    )
}
