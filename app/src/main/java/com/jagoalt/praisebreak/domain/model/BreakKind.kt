package com.jagoalt.praisebreak.domain.model

enum class BreakKind(val title: String, val blurb: String) {
    Scripture("Bible verse", ""),
    Prayer("Prayer", ""),
    Stillness("Mental break", ""),
    Thanks("Give thanks", "")
}
