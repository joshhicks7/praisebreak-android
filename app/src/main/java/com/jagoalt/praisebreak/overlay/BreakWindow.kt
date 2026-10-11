package com.jagoalt.praisebreak.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jagoalt.praisebreak.R
import com.jagoalt.praisebreak.domain.model.BreakFace
import com.jagoalt.praisebreak.domain.model.CoverStyle
import com.jagoalt.praisebreak.ui.breakcard.BreakCard
import com.jagoalt.praisebreak.ui.theme.PraiseTheme
import kotlin.math.min

class BreakOverlayState {
    var session by mutableStateOf<BreakFace?>(null)
    var nowMs by mutableLongStateOf(0L)
}

class BreakWindow(private val context: Context) {
    val state = BreakOverlayState()
    var onContinue: () -> Unit = {}
    var onSnooze: () -> Unit = {}

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val params = createParams()
    private var host: OverlayHost? = null
    private var composeView: ComposeView? = null

    fun render(face: BreakFace, nowMs: Long) {
        state.session = face
        state.nowMs = nowMs
        if (!ensure()) return
        applyFrame(face.style)
    }

    fun hide() {
        state.session = null
        val view = composeView ?: return
        params.width = 0
        params.height = 0
        params.flags = popupFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        if (!view.isAttachedToWindow) return
        runCatching { windowManager?.updateViewLayout(view, params) }
    }

    fun detach() {
        val view = composeView
        composeView = null
        host?.destroy()
        host = null
        if (view != null) {
            runCatching { windowManager?.removeView(view) }
        }
    }

    private fun ensure(): Boolean {
        if (composeView != null) return true
        val windows = windowManager ?: return false
        if (!Settings.canDrawOverlays(context)) return false
        val overlayHost = OverlayHost()
        val themed = ContextThemeWrapper(context, R.style.Theme_PraiseBreak)
        val view = ComposeView(themed).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(overlayHost)
            setViewTreeSavedStateRegistryOwner(overlayHost)
            setViewTreeViewModelStoreOwner(overlayHost)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent { OverlayBody(state, { onContinue() }, { onSnooze() }) }
        }
        host = overlayHost
        overlayHost.start()
        val added = runCatching { windows.addView(view, params) }.isSuccess
        if (!added) {
            overlayHost.destroy()
            host = null
            return false
        }
        composeView = view
        overlayHost.resume()
        return true
    }

    private fun applyFrame(style: CoverStyle) {
        params.flags = if (style == CoverStyle.Freeze) freezeFlags else popupFlags
        params.gravity = Gravity.CENTER
        if (style == CoverStyle.Freeze) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            params.width = min(screenWidth() - dp(32), dp(420)).coerceAtLeast(dp(280))
            params.height = min(screenHeight() - dp(80), dp(560)).coerceAtLeast(dp(320))
        }
        val view = composeView ?: return
        if (!view.isAttachedToWindow) return
        runCatching { windowManager?.updateViewLayout(view, params) }
    }

    private fun createParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            0,
            0,
            type,
            popupFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun screenWidth() = context.resources.displayMetrics.widthPixels

    private fun screenHeight() = context.resources.displayMetrics.heightPixels

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
    }

    companion object {
        private val popupFlags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

        private val freezeFlags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    }
}

@Composable
private fun OverlayBody(
    state: BreakOverlayState,
    onContinue: () -> Unit,
    onSnooze: () -> Unit
) {
    val face = state.session ?: return
    val freeze = face.style == CoverStyle.Freeze
    PraiseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (freeze) Color(0xF20C1416) else Color.Transparent)
                .then(
                    if (freeze) {
                        Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                            .systemBarsPadding()
                            .padding(24.dp)
                    } else {
                        Modifier.padding(12.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            BreakCard(
                face = face,
                nowMs = state.nowMs,
                onContinue = onContinue,
                onSnooze = onSnooze
            )
        }
    }
}
