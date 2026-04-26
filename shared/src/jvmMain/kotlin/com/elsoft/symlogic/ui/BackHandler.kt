package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM desktop, as there's no system-level back button.
    // A back button in the UI would be a regular Composable.
}
