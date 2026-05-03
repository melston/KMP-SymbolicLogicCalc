package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp // Import the dp extension function
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState

@Composable
actual fun MovableInputDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Window(
        onCloseRequest = onDismissRequest,
        title = "Add Proof Step",
        state = rememberWindowState(width = 400.dp, height = 350.dp) // Now dp() will resolve correctly
    ) {
        content()
    }
}
