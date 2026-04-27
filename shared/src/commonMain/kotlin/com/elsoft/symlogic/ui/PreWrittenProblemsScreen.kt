package com.elsoft.symlogic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.ProblemDefinition
import com.elsoft.symlogic.problems.ProblemSet
import com.elsoft.symlogic.problems.getProblemSetRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreWrittenProblemsScreen(onBack: () -> Unit, onSolve: (ProblemDefinition) -> Unit) {
    val problemSetRepository = remember { getProblemSetRepository() }
    val coroutineScope = rememberCoroutineScope()

    var availableSetNames by remember { mutableStateOf(emptyList<String>()) }
    var selectedProblemSet by remember { mutableStateOf<ProblemSet?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load the list of available problem sets when the screen is first shown
    LaunchedEffect(Unit) {
        availableSetNames = problemSetRepository.listProblemSetNames()
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
            // Left Pane: List of available problem sets
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

            // Right Pane: Details of the selected problem set
            Column(modifier = Modifier.weight(0.6f).padding(16.dp)) {
                selectedProblemSet?.let { set ->
                    Text(set.name, style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(set.problems) { problem ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSolve(problem) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(problem.id, fontWeight = FontWeight.Bold)
                                Text("Prove: ${problem.conclusion}", style = MaterialTheme.typography.body2)
                            }
                            Divider()
                        }
                    }
                } ?: run {
                    Text("Select a problem set from the left to see its problems.")
                }
            }
        }
        
        errorMessage?.let {
            // A more robust error display might use a Snackbar
            Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(16.dp))
        }
    }
}
