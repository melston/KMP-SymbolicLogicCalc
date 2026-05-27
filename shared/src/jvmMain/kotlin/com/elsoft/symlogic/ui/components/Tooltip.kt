package com.elsoft.symlogic.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun Tooltip(
    text: String,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = {
            Surface(
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) {
        content()
    }
}
