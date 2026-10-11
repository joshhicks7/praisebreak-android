package com.jagoalt.praisebreak.data.apps

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process

data class LaunchableApp(val packageName: String, val label: String)

data class AppUse(val packageName: String, val label: String, val minutes: Int)

object InstalledApps {
    fun load(context: Context): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(intent, 0)
            }
        }.getOrDefault(emptyList())
        return resolved
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == context.packageName) return@mapNotNull null
                val label = info.loadLabel(context.packageManager)?.toString().orEmpty().ifBlank { pkg }
                LaunchableApp(pkg, label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}

object UsageAccess {
    fun granted(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun today(context: Context): List<AppUse> {
        if (!granted(context)) return emptyList()
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val stats = runCatching {
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, System.currentTimeMillis())
        }.getOrNull().orEmpty()
        val totals = HashMap<String, Long>()
        for (stat in stats) {
            if (stat.packageName == context.packageName) continue
            val time = if (Build.VERSION.SDK_INT >= 29) stat.totalTimeVisible else stat.totalTimeInForeground
            totals[stat.packageName] = (totals[stat.packageName] ?: 0) + time
        }
        return totals
            .map { (pkg, ms) -> AppUse(pkg, label(context, pkg), (ms / 60_000L).toInt()) }
            .filter { it.minutes > 0 }
            .sortedByDescending { it.minutes }
    }

    private fun label(context: Context, packageName: String): String {
        return runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }
}
