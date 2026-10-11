package com.jagoalt.praisebreak.graph

import android.content.Context
import com.jagoalt.praisebreak.data.SettingsStore
import com.jagoalt.praisebreak.data.bible.BibleRepository
import com.jagoalt.praisebreak.data.bible.VersionCache

class AppGraph private constructor(context: Context) {
    val settings = SettingsStore(context)
    val bibles = BibleRepository()
    val versionCache = VersionCache(context)

    companion object {
        @Volatile
        private var instance: AppGraph? = null

        fun of(context: Context): AppGraph {
            return instance ?: synchronized(this) {
                instance ?: AppGraph(context.applicationContext).also { instance = it }
            }
        }
    }
}
