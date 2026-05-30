package com.elsoft.symlogic.ui.components

import androidx.compose.runtime.Composable

/**
 * A platform-specific button that opens a file picker and returns the file content as a string.
 */
@Composable
expect fun FilePickerButton(onFilePicked: (String) -> Unit)
