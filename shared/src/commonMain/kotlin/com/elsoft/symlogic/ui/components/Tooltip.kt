package com.elsoft.symlogic.ui.components

import androidx.compose.runtime.Composable

/**
 * A platform-specific tooltip wrapper.
 * On Desktop, this will show a tooltip when the user hovers over the content.
 * On other platforms, it will simply display the content.
 *
 * @param text The text to display in the tooltip.
 * @param content The composable content that the tooltip is attached to.
 */
@Composable
expect fun Tooltip(
    text: String,
    content: @Composable () -> Unit
)
