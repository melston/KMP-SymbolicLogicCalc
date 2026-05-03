package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
actual fun MovableInputDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        content = content
    )
}
