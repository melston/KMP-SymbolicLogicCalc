package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.logic.*
import com.elsoft.symlogic.problems.ProofEngine
import com.elsoft.symlogic.problems.ProblemDefinition
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GeneratedProblemsScreen(onBack: () -> Unit, onSolve: (ProblemDefinition) -> Unit) {
    val proofEngine = remember { ProofEngine(Random.Default) }
    val coroutineScope = rememberCoroutineScope()
    
    val allRules = remember { AllRulesOfInference + AllRulesOfReplacement }
    var selectedRules by remember { mutableStateOf(emptySet<Rule>()) }
    var targetSteps by remember { mutableStateOf(3) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generated Problems") },
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
            Text(
                text = "Select Rules to Focus On:",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allRules) { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedRules.contains(rule),
                            onCheckedChange = { isChecked ->
                                selectedRules = if (isChecked) selectedRules + rule else selectedRules - rule
                            }
                        )
                        Text(rule.name)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = targetSteps.toString(),
                onValueChange = { targetSteps = it.toIntOrNull() ?: 0 },
                label = { Text("Target Solution Steps") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    isGenerating = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val problem = proofEngine.generateProblem(
                                targetSteps = targetSteps.coerceAtLeast(1),
                                requiredRules = selectedRules.toList()
                            )
                            onSolve(problem)
                        } catch (e: Exception) {
                            errorMessage = e.message
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
                } else {
                    Text("Generate Problem")
                }
            }
        }
    }
}
