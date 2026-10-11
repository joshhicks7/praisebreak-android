package com.jagoalt.praisebreak.service

import com.jagoalt.praisebreak.domain.model.BreakKind
import com.jagoalt.praisebreak.domain.model.BreakFace
import com.jagoalt.praisebreak.domain.model.CoverStyle

data class OpenBreak(
    val token: Long,
    val kind: BreakKind,
    val style: CoverStyle,
    val endsAt: Long,
    val durationMs: Long,
    val body: String,
    val reference: String,
    val loading: Boolean,
    val forced: Boolean
) {
    fun face(appLabel: String): BreakFace {
        return BreakFace(
            kind = kind,
            style = style,
            endsAtMs = endsAt,
            durationMs = durationMs,
            appLabel = appLabel,
            body = body,
            reference = reference,
            loading = loading,
            allowSnooze = false
        )
    }
}
