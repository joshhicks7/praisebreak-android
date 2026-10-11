package com.jagoalt.praisebreak.ui.timing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jagoalt.praisebreak.data.apps.AppUse
import com.jagoalt.praisebreak.ui.components.QuietCard

private val SliceColors = listOf(
    Color(0xFF8ED4CE),
    Color(0xFFF0A08C),
    Color(0xFFE7C56A),
    Color(0xFF7EB6E0),
    Color(0xFFC7A6E0),
    Color(0xFF8FA3A1)
)

private data class Slice(
    val label: String,
    val minutes: Int,
    val percent: Int,
    val color: Color
)

@Composable
fun MostUsed(usage: List<AppUse>) {
    val slices = slices(usage)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Most used",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        if (slices.isEmpty()) {
            Text(
                "No app time yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            UsageCircle(slices)
            QuietCard {
                slices.forEach { slice ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Canvas(Modifier.size(12.dp)) { drawCircle(slice.color) }
                        Text(slice.label, modifier = Modifier.weight(1f))
                        Text(
                            "${slice.percent}% · ${durationLabel(slice.minutes)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageCircle(slices: List<Slice>) {
    Canvas(modifier = Modifier.size(220.dp)) {
        val total = slices.sumOf { it.minutes }.coerceAtLeast(1)
        var angle = -90f
        slices.forEach { slice ->
            val sweep = slice.minutes * 360f / total
            drawArc(
                color = slice.color,
                startAngle = angle,
                sweepAngle = sweep,
                useCenter = true
            )
            angle += sweep
        }
        drawCircle(
            color = Color(0xFF162022),
            radius = size.minDimension * 0.28f
        )
        drawCircle(
            color = Color(0xFF2C4446),
            radius = size.minDimension * 0.28f,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun slices(usage: List<AppUse>): List<Slice> {
    val ranked = usage.filter { it.minutes > 0 }.sortedByDescending { it.minutes }
    val total = ranked.sumOf { it.minutes }
    if (total == 0) return emptyList()
    val top = ranked.take(5)
    val restMinutes = ranked.drop(5).sumOf { it.minutes }
    val rows = buildList {
        top.forEach { add(it.label to it.minutes) }
        if (restMinutes > 0) add("Other" to restMinutes)
    }
    val raw = rows.map { (it.second * 100) / total }
    val leftover = 100 - raw.sum()
    return rows.mapIndexed { index, (label, minutes) ->
        val percent = raw[index] + if (index == 0) leftover else 0
        Slice(label, minutes, percent.coerceAtLeast(0), SliceColors[index % SliceColors.size])
    }
}

fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours <= 0 -> "$rest min"
        rest == 0 -> "$hours h"
        else -> "$hours h $rest min"
    }
}
