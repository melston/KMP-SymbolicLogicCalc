package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable

@Composable
actual fun FilePickerButton(onFilePicked: (String) -> Unit) {
    // On Android, a file picker from a shared module is complex.
    // The primary input method is pasting text.
    // Therefore, we render nothing here for now.
}
