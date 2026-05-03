package com.elsoft.symlogic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.ProblemDefinition
import com.elsoft.symlogic.problems.ProblemSet
import com.elsoft.symlogic.problems.Proof
import com.elsoft.symlogic.problems.getProblemSetRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreWrittenProblemsScreen(onBack: () -> Unit, onSolve: (ProblemDefinition) -> Unit) {
    val problemSetRepository = remember { getProblemSetRepository() }
    val coroutineScope = rememberCoroutineScope()

    var availableSetNames by remember { mutableStateOf(emptyList<String>()) }
    var selectedProblemSet by remember { mutableStateOf<ProblemSet?>(null) }
    var solvedProblemIds by remember { mutableStateOf(emptySet<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        availableSetNames = problemSetRepository.listProblemSetNames()
    }

    LaunchedEffect(selectedProblemSet) {
        selectedProblemSet?.let {
            solvedProblemIds = problemSetRepository.listSolvedProblemIds(it.name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-written Problems") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.weight(0.4f).padding(16.dp)) {
                Text("Available Sets", style = MaterialTheme.typography.h6)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(availableSetNames) { name ->
                        Text(
                            text = name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        selectedProblemSet = problemSetRepository.loadProblemSet(name)
                                    }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(0.6f).padding(16.dp)) {
                selectedProblemSet?.let { set ->
                    Text(set.name, style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(set.problems) { problem ->
                            val isSolved = solvedProblemIds.contains(problem.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSolve(problem) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSolved) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Solved",
                                        tint = Color.Green,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Column {
                                    Text(problem.id, fontWeight = FontWeight.Bold)
                                    Text("Prove: ${problem.conclusion}", style = MaterialTheme.typography.body2)
                                }
                            }
                            Divider()
                        }
                    }
                } ?: Text("Select a problem set from the left to see its problems.")
            }
        }
        
        errorMessage?.let {
            Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(16.dp))
        }
    }
}
