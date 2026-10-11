package com.jagoalt.praisebreak.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jagoalt.praisebreak.R

private const val YOUVERSION = "https://www.youversion.com/"
private const val PHONE_DIGITS = "5013510651"
private const val TEXT_BODY =
    "Hi — I'm using Praise Break and would like to connect. I'm interested in learning more about Jesus."

@Composable
fun AboutRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Spacer(Modifier.size(12.dp))
        Text(
            "Thank you for trying Praise Break. If you're like us, you realized you were spending too much time scrolling endless feeds and you needed a mental break to ground yourself and focus on Christ.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.size(28.dp))
        Text(
            "If you would like to learn more about Jesus or find a church, give us a call or text.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.size(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactIcon(R.drawable.ic_call, "Call") {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_DIGITS")))
            }
            Spacer(Modifier.width(28.dp))
            ContactIcon(R.drawable.ic_text, "Text") {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$PHONE_DIGITS")).apply {
                        putExtra("sms_body", TEXT_BODY)
                    }
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Shoutout {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(YOUVERSION)))
            }
        }
        Spacer(Modifier.size(16.dp))
    }
}

@Composable
private fun ContactIcon(icon: Int, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(72.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun Shoutout(onYouVersion: () -> Unit) {
    val line = buildAnnotatedString {
        append("Shoutout ")
        pushStringAnnotation("link", YOUVERSION)
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("YouVersion")
        }
        pop()
        append(" for the scripture.")
    }
    ClickableText(
        text = line,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth(),
        onClick = { offset ->
            if (line.getStringAnnotations("link", offset, offset).isNotEmpty()) onYouVersion()
        }
    )
}
