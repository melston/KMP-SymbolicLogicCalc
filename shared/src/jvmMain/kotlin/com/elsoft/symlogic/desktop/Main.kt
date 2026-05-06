package com.elsoft.symlogic.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.elsoft.symlogic.ui.App
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.debounce
import java.util.prefs.Preferences

fun main() = application {
    val prefs = Preferences.userRoot().node("com.elsoft.symlogic")
    val appSettings = PreferencesSettings(prefs)

    val windowState = rememberWindowState(
        position = WindowPosition(
            (prefs.getInt("window_x", 100)).dp,
            (prefs.getInt("window_y", 100)).dp
        ),
        size = DpSize(
            (prefs.getInt("window_width", 1200)).dp,
            (prefs.getInt("window_height", 800)).dp
        ),
        placement = if (prefs.getBoolean("window_maximized", false)) {
            WindowPlacement.Maximized
        } else {
            WindowPlacement.Floating
        }
    )

    LaunchedEffect(windowState) {
        snapshotFlow { windowState.size }
            .debounce(500)
            .collect {
                appSettings["window_width"] = it.width.value.toInt()
                appSettings["window_height"] = it.height.value.toInt()
            }
    }

    LaunchedEffect(windowState) {
        snapshotFlow { windowState.position }
            .debounce(500)
            .collect {
                if (windowState.placement == WindowPlacement.Floating) {
                    appSettings["window_x"] = it.x.value.toInt()
                    appSettings["window_y"] = it.y.value.toInt()
                }
            }
    }
    
    LaunchedEffect(windowState) {
        snapshotFlow { windowState.placement }
            .collect {
                appSettings["window_maximized"] = it == WindowPlacement.Maximized
            }
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Symbolic Logic Game"
    ) {
        App()
    }
}
