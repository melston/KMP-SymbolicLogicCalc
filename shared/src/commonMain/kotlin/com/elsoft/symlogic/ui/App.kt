package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }

        when (currentScreen) {
            Screen.MAIN_MENU -> MainMenuScreen(onNavigate = { screen -> currentScreen = screen })
            Screen.GENERATED_PROBLEMS -> Text("Generated Problems Screen (Coming Soon!)") // Placeholder
            Screen.PRE_WRITTEN_PROBLEMS -> Text("Pre-written Problems Screen (Coming Soon!)") // Placeholder
            Screen.SOLVER -> Text("Solver Screen (Coming Soon!)") // Placeholder
        }
    }
}

@Composable
fun MainMenuScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Symbolic Logic Game",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onNavigate(Screen.GENERATED_PROBLEMS) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Practice with Generated Problems")
        }

        Button(
            onClick = { onNavigate(Screen.PRE_WRITTEN_PROBLEMS) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Solve Pre-written Problems")
        }
    }
}
