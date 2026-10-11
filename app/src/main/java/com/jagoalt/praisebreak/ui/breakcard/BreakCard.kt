package com.jagoalt.praisebreak.ui.breakcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jagoalt.praisebreak.domain.model.BreakFace
import com.jagoalt.praisebreak.domain.model.BreakKind

@Composable
fun BreakCard(
    face: BreakFace,
    nowMs: Long,
    onContinue: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = (face.endsAtMs - nowMs).coerceAtLeast(0)
    val progress = if (face.durationMs <= 0L) {
        0f
    } else {
        remaining.toFloat() / face.durationMs.toFloat()
    }
    val finished = remaining == 0L && !face.loading
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (face.appLabel.isNotBlank()) {
                Text(
                    text = face.appLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = heading(face),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (face.kind == BreakKind.Scripture && face.body.isNotBlank()) {
                Text(
                    text = face.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = clock(remaining),
                    style = MaterialTheme.typography.titleMedium
                )
                if (face.allowSnooze) {
                    TextButton(onClick = onSnooze, enabled = !finished) {
                        Text("Not now")
                    }
                }
            }
            Button(
                onClick = onContinue,
                enabled = finished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (finished) "Continue" else "Stay here")
            }
        }
    }
}

private fun heading(face: BreakFace): String {
    return when (face.kind) {
        BreakKind.Prayer -> "Pray break 🙏"
        BreakKind.Scripture -> face.reference
        else -> face.kind.title
    }
}

private fun clock(ms: Long): String {
    val total = (ms / 1000L).toInt()
    return "%d:%02d".format(total / 60, total % 60)
}
