package com.jagoalt.praisebreak.domain.model

data class PraiseSettings(
    val enabled: Boolean = false,
    val packages: Set<String> = emptySet(),
    val days: Int = Weekday.all,
    val startMinute: Int = 8 * 60,
    val endMinute: Int = 22 * 60,
    val everyMinutes: Int = 20,
    val breakSeconds: Int = 60,
    val style: CoverStyle = CoverStyle.Popup,
    val kinds: Set<BreakKind> = BreakKind.values().toSet(),
    val bibleId: Int = 3034,
    val bibleAbbreviation: String = "BSB",
    val bibleTitle: String = "Berean Standard Bible",
    val bibleAttribution: String = "",
    val apiKey: String = ""
) {
    fun resolvedKey(fallback: String): String = apiKey.trim().ifBlank { fallback.trim() }

    fun watches(packageName: String?): Boolean {
        return !packageName.isNullOrBlank() && packageName in packages
    }
}
