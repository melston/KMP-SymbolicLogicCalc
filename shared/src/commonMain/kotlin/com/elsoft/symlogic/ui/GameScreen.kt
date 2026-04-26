package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(isDesktop: Boolean) {
    Scaffold(
        bottomBar = {
            if (!isDesktop) {
                // Mobile: Palette sits at the bottom
                LogicPalette(onSymbolSelected = { /* update state */ })
            }
        }
    ) { padding ->
        Row(Modifier.padding(padding)) {
            // Main Game Area
            Box(Modifier.weight(1f)) { /* Your Logic Board */ }

            if (isDesktop) {
                // Desktop: Palette sits on the right
                LogicPalette(
                    onSymbolSelected = { /* update state */ },
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}