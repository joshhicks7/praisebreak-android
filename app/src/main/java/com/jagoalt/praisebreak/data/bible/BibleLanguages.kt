package com.jagoalt.praisebreak.data.bible

import java.util.Locale

object BibleLanguages {
    const val ENGLISH = "en"

    fun code(locale: Locale = Locale.getDefault()): String = normalize(locale.language)

    fun normalize(raw: String): String {
        val language = raw.trim().lowercase(Locale.ROOT).substringBefore('-').substringBefore('_')
        val mapped = when (language) {
            "iw" -> "he"
            "ji" -> "yi"
            "in" -> "id"
            else -> language
        }
        if (mapped.isBlank() || mapped == "und" || !mapped.matches(CODE)) return ENGLISH
        return mapped
    }

    private val CODE = Regex("[a-z]{2,3}")
}
