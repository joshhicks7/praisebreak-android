package com.jagoalt.praisebreak.access

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.jagoalt.praisebreak.data.apps.UsageAccess
import com.jagoalt.praisebreak.service.AppWatchService

data class BreakAccessState(
    val cover: Boolean,
    val whichApp: Boolean,
    val today: Boolean
) {
    val canInterrupt: Boolean get() = cover && whichApp
}

object BreakAccess {
    fun read(context: Context): BreakAccessState {
        return BreakAccessState(
            cover = Settings.canDrawOverlays(context),
            whichApp = serviceEnabled(context),
            today = UsageAccess.granted(context)
        )
    }

    fun coverSettings(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    fun whichAppSettings(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun todaySettings(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    private fun serviceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, AppWatchService::class.java)
        val raw = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return raw.split(':')
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .any { it == expected }
    }
}
