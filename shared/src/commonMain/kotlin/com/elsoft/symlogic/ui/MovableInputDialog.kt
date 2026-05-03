package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable

/**
 * A platform-specific dialog for input.
 * On Desktop, this will be a movable window.
 * On Android, this will be a standard modal dialog.
 */
@Composable
expect fun MovableInputDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
)
