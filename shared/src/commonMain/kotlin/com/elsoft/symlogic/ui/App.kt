package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.ProblemDefinition
import com.elsoft.symlogic.problems.Proof

@Composable
fun App() {
    MaterialTheme {
        val navigationStateHolder = remember { NavigationStateHolder(Screen.MainMenu) }

        // Handle back press for Android
        BackHandler(enabled = navigationStateHolder.backStack.size > 1) {
            navigationStateHolder.goBack()
        }

        when (val currentScreen = navigationStateHolder.currentScreen()) {
            Screen.MainMenu -> MainMenuScreen(onNavigate = navigationStateHolder::navigateTo)
            Screen.GeneratedProblems -> GeneratedProblemsScreen(
                onBack = navigationStateHolder::goBack,
                onSolve = { problem -> navigationStateHolder.navigateTo(Screen.Solver(problem)) }
            )
            Screen.PreWrittenProblems -> PreWrittenProblemsScreen(
                onBack = navigationStateHolder::goBack,
                onSolve = { problem -> navigationStateHolder.navigateTo(Screen.Solver(problem)) }
            )
            Screen.ImportProblemSet -> ImportProblemSetScreen(
                onBack = navigationStateHolder::goBack
            )
            is Screen.Solver -> GameScreen(
                initialProof = Proof(currentScreen.problem)
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

        Button(
            onClick = { onNavigate(Screen.GeneratedProblems) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Practice with Generated Problems")
        }

        Button(
            onClick = { onNavigate(Screen.PreWrittenProblems) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Solve Pre-written Problems")
        }

        Button(
            onClick = { onNavigate(Screen.ImportProblemSet) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Import Problem Set")
        }
    }
}

@Composable
fun SolverScreen(problem: ProblemDefinition, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Solver Screen for: ${problem.id}")
        Text("Prove: ${problem.conclusion}")
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
