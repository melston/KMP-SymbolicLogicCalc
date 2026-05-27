package com.elsoft.symlogic.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun Tooltip(
    text: String,
    content: @Composable () -> Unit
) {
    // Tooltips on hover are not a standard mobile UI pattern,
    // so we just display the content directly.
    content()
}
