package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.Proof
import com.elsoft.symlogic.problems.ProblemDefinition

@Composable
fun App() {
    MaterialTheme {
        val navigationStateHolder = remember { NavigationStateHolder(Screen.MainMenu) }

        BackHandler(enabled = navigationStateHolder.backStack.size > 1) {
            navigationStateHolder.goBack()
        }

        when (val currentScreen = navigationStateHolder.currentScreen()) {
            Screen.MainMenu -> MainMenuScreen(onNavigate = navigationStateHolder::navigateTo)
            Screen.GeneratedProblems -> GeneratedProblemsScreen(
                onBack = navigationStateHolder::goBack,
                onSolve = { problem ->
                    // Pass "Generated" as the setName for generated problems
                    navigationStateHolder.navigateTo(Screen.Solver(Proof(problem), "Generated"))
                }
            )
            Screen.PreWrittenProblems -> PreWrittenProblemsScreen(
                onBack = navigationStateHolder::goBack,
                onSolve = { proof, setName ->
                    navigationStateHolder.navigateTo(Screen.Solver(proof, setName))
                }
            )
            Screen.ImportProblemSet -> ImportProblemSetScreen(
                onBack = navigationStateHolder::goBack
            )
            is Screen.Solver -> GameScreen(
                initialProof = currentScreen.proof,
                setName = currentScreen.setName,
                onBack = navigationStateHolder::goBack
            )
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
        Button(onClick = { onNavigate(Screen.GeneratedProblems) }) {
            Text("Practice with Generated Problems")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onNavigate(Screen.PreWrittenProblems) }) {
            Text("Solve Pre-written Problems")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onNavigate(Screen.ImportProblemSet) }) {
            Text("Import Problem Set")
        }
    }
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
