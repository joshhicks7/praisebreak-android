package com.jagoalt.praisebreak.data.bible

import android.content.Context
import com.jagoalt.praisebreak.domain.model.BibleVersion
import org.json.JSONArray
import org.json.JSONObject

class VersionCache(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun read(language: String, nowMs: Long = System.currentTimeMillis()): List<BibleVersion>? {
        if (prefs.getString(KEY_LANG, null) != BibleLanguages.normalize(language)) return null
        val savedAt = prefs.getLong(KEY_AT, 0L)
        if (!VersionShelf.fresh(savedAt, nowMs)) return null
        val raw = prefs.getString(KEY_BODY, null) ?: return null
        return VersionShelf.decode(raw).takeIf { it.isNotEmpty() }
    }

    fun write(language: String, versions: List<BibleVersion>, nowMs: Long = System.currentTimeMillis()) {
        if (versions.isEmpty()) return
        prefs.edit()
            .putString(KEY_LANG, BibleLanguages.normalize(language))
            .putLong(KEY_AT, nowMs)
            .putString(KEY_BODY, VersionShelf.encode(versions))
            .apply()
    }

    private companion object {
        const val FILE = "bible_versions"
        const val KEY_LANG = "language"
        const val KEY_AT = "saved_at"
        const val KEY_BODY = "versions"
    }
}

object VersionShelf {
    const val WEEK_MS = 7L * 24 * 60 * 60 * 1000

    fun fresh(savedAtMs: Long, nowMs: Long): Boolean {
        return savedAtMs > 0L && nowMs - savedAtMs < WEEK_MS
    }

    fun encode(versions: List<BibleVersion>): String {
        val array = JSONArray()
        versions.forEach { version ->
            array.put(
                JSONObject()
                    .put("id", version.id)
                    .put("abbreviation", version.abbreviation)
                    .put("title", version.title)
                    .put("language", version.language)
                    .put("copyright", version.copyright)
            )
        }
        return array.toString()
    }

    fun decode(raw: String): List<BibleVersion> {
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optInt("id", 0)
                if (id == 0) continue
                add(
                    BibleVersion(
                        id = id,
                        abbreviation = item.optString("abbreviation"),
                        title = item.optString("title"),
                        language = item.optString("language"),
                        copyright = item.optString("copyright")
                    )
                )
            }
        }
    }
}
