package com.elsoft.symlogic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.getProblemSetRepository
import com.elsoft.symlogic.problems.ProblemDefinition
import com.elsoft.symlogic.problems.ProblemSet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreWrittenProblemsScreen(onBack: () -> Unit, onSolve: (ProblemDefinition) -> Unit) {
    val problemSetRepository = remember { getProblemSetRepository() }
    val coroutineScope = rememberCoroutineScope()

    var loadedProblemSets by remember { mutableStateOf(listOf<ProblemSet>()) }
    var filenameInput by remember { mutableStateOf("copi_problems.json") } // Default filename
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Load ProblemSet UI
            OutlinedTextField(
                value = filenameInput,
                onValueChange = { filenameInput = it },
                label = { Text("Problem Set Filename") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    errorMessage = null
                    coroutineScope.launch {
                        val loadedSet = problemSetRepository.loadProblemSet(filenameInput)
                        if (loadedSet != null) {
                            loadedProblemSets = (loadedProblemSets + loadedSet).distinctBy { it.name }
                        } else {
                            errorMessage = "Failed to load problem set from '$filenameInput'."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Problem Set")
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Available Problem Sets:",
                style = MaterialTheme.typography.subtitle1, // Changed from h6 to subtitle1 for consistency with other headers
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // List of loaded ProblemSets and their problems
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (loadedProblemSets.isEmpty()) {
                    item {
                        Text("No problem sets loaded yet. Try loading one!", modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(loadedProblemSets) { problemSet ->
                        var expanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { expanded = !expanded }
                        ) {
                            Text(
                                text = problemSet.name,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            if (expanded) {
                                problemSet.problems.forEach { problem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSolve(problem) }
                                            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = problem.id)
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = "Prove: ${problem.conclusion}", style = MaterialTheme.typography.caption)
                                    }
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
