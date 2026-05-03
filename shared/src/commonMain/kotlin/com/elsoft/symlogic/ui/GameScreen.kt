package com.elsoft.symlogic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.problems.Proof
import com.elsoft.symlogic.problems.ProofValidator
import com.elsoft.symlogic.problems.ValidationResult
import com.elsoft.symlogic.problems.getProblemSetRepository
import com.elsoft.symlogic.problems.parsers.ExpressionParser
import kotlinx.coroutines.launch

@Composable
fun GameScreen(initialProof: Proof, onBack: () -> Unit) {
    var proof by remember { mutableStateOf(initialProof) }
    val validator = remember { ProofValidator() }
    val expressionParser = remember { ExpressionParser() }
    val repository = remember { getProblemSetRepository() }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var validationError by remember { mutableStateOf<String?>(null) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showProofCompleteDialog by remember { mutableStateOf(false) }
    var selectedStepIds by remember { mutableStateOf(emptySet<Int>()) }

    val indentationLevel = proof.steps.count { it is Proof.ProofStep.Assumption } -
                           proof.steps.count { it is Proof.ProofStep.ImplicationIntroductionStep }
    val isSubProofActive = indentationLevel > 0

    // Pre-calculate all indentation levels. This will only re-run when the proof.steps list changes.
    val stepIndentationLevels = remember(proof.steps) {
        val levels = mutableListOf<Int>()
        var currentIndent = 0
        for (step in proof.steps) {
            if (step is Proof.ProofStep.ImplicationIntroductionStep) currentIndent--
            levels.add(currentIndent.coerceAtLeast(0))
            if (step is Proof.ProofStep.Assumption) currentIndent++
        }
        levels
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(proof.problem.id) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            repository.saveProof(proof)
                            scaffoldState.snackbarHostState.showSnackbar("Proof Saved!")
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Proof")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showInputDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Proof Step")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Proof Display
            Text("Premises:", style = MaterialTheme.typography.h6)
            proof.problem.premises.forEachIndexed { index, premise ->
                val id = index + 1
                ProofStepRow(
                    id = id,
                    expression = premise.toString(),
                    justification = "Premise",
                    indentationLevel = 0,
                    isSelected = selectedStepIds.contains(id),
                    onToggleSelection = {
                        selectedStepIds = if (selectedStepIds.contains(id)) selectedStepIds - id else selectedStepIds + id
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Divider(thickness = 2.dp)
            Spacer(Modifier.height(8.dp))
            Text("Prove: ${proof.problem.conclusion}", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(proof.steps.size) { index ->
                    val step = proof.steps[index]
                    val justification = when (step) {
                        is Proof.ProofStep.RegularStep -> "${step.rule.name} ${step.parentStepIds.joinToString()}"
                        is Proof.ProofStep.Assumption -> "Assumption"
                        is Proof.ProofStep.ImplicationIntroductionStep -> "II ${step.assumptionIds.joinToString()}-${step.conclusionOfSubProofId}"
                    }
                    ProofStepRow(
                        id = step.id,
                        expression = step.expression.toString(),
                        justification = justification,
                        indentationLevel = stepIndentationLevels[index],
                        isSelected = selectedStepIds.contains(step.id),
                        onToggleSelection = { id ->
                            selectedStepIds = if (selectedStepIds.contains(id)) selectedStepIds - id else selectedStepIds + id
                        }
                    )
                }
            }
        }

        // Input Dialog and other logic remains the same...
        if (showInputDialog) {
            val selectedExpressions = remember(selectedStepIds, proof) {
                val allSteps = (proof.problem.premises.mapIndexed { index, expr -> (index + 1) to expr } +
                                proof.steps.map { it.id to it.expression }).toMap()
                selectedStepIds.mapNotNull { id -> allSteps[id]?.let { id to it } }.toMap()
            }

            Dialog(onDismissRequest = { showInputDialog = false }) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Step", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(16.dp))
                        
                        ProofInput(
                            selectedParentExpressions = selectedExpressions,
                            isSubProofActive = isSubProofActive,
                            onAddStep = { expressionStr, rule, parentIdsStr ->
                                validationError = null
                                try {
                                    val expression = expressionParser.parse(expressionStr)
                                    val parentIds = parentIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                                    val newProof = proof.addStep(expression, rule, parentIds)
                                    
                                    when (val result = validator.validate(newProof)) {
                                        is ValidationResult.Complete -> {
                                            proof = newProof
                                            showInputDialog = false
                                            selectedStepIds = emptySet()
                                            showProofCompleteDialog = true
                                        }
                                        is ValidationResult.ValidSoFar -> {
                                            proof = newProof
                                            showInputDialog = false
                                            selectedStepIds = emptySet()
                                        }
                                        is ValidationResult.Invalid -> validationError = "Error in step ${result.stepId}: ${result.reason}"
                                    }
                                } catch (e: Exception) {
                                    validationError = e.message
                                }
                            },
                            onAddAssumption = { expressionStr ->
                                validationError = null
                                try {
                                    val expression = expressionParser.parse(expressionStr)
                                    val newProof = proof.addAssumption(expression)
                                    
                                    when (val result = validator.validate(newProof)) {
                                        is ValidationResult.ValidSoFar -> {
                                            proof = newProof
                                            showInputDialog = false
                                            selectedStepIds = emptySet()
                                        }
                                        is ValidationResult.Invalid -> validationError = "Error in step ${result.stepId}: ${result.reason}"
                                        is ValidationResult.Complete -> {} 
                                    }
                                } catch (e: Exception) {
                                    validationError = e.message
                                }
                            },
                            onCloseSubProof = {
                                validationError = null
                                try {
                                    val activeAssumptions = proof.steps.filterIsInstance<Proof.ProofStep.Assumption>()
                                        .filterNot { assumption ->
                                            proof.steps.filterIsInstance<Proof.ProofStep.ImplicationIntroductionStep>()
                                                .any { it.assumptionIds.contains(assumption.id) }
                                        }
                                    
                                    if (activeAssumptions.isNotEmpty() && proof.steps.isNotEmpty()) {
                                        val newProof = proof.addImplicationIntroductionStep(
                                            assumptionIds = activeAssumptions.map { it.id },
                                            conclusionId = proof.steps.last().id
                                        )
                                        
                                        when (val result = validator.validate(newProof)) {
                                            is ValidationResult.Complete -> {
                                                proof = newProof
                                                showInputDialog = false
                                                selectedStepIds = emptySet()
                                                showProofCompleteDialog = true
                                            }
                                            is ValidationResult.ValidSoFar -> {
                                                proof = newProof
                                                showInputDialog = false
                                                selectedStepIds = emptySet()
                                            }
                                            is ValidationResult.Invalid -> validationError = "Error closing sub-proof: ${result.reason}"
                                        }
                                    } else {
                                        validationError = "No active sub-proof to close."
                                    }
                                } catch (e: Exception) {
                                    validationError = e.message
                                }
                            }
                        )
                        
                        validationError?.let {
                            Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }

        if (showProofCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showProofCompleteDialog = false },
                title = { Text("Congratulations!") },
                text = { Text("You have successfully completed the proof.") },
                confirmButton = {
                    Button(onClick = { showProofCompleteDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ProofStepRow(
    id: Int,
    expression: String,
    justification: String,
    indentationLevel: Int,
    isSelected: Boolean,
    onToggleSelection: (Int) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onToggleSelection(id) }
            .padding(start = (indentationLevel * 24).dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text("$id.", modifier = Modifier.width(40.dp), fontFamily = FontFamily.Monospace)
            Text(expression, fontFamily = FontFamily.Monospace)
        }
        Text(justification, fontFamily = FontFamily.Monospace, color = Color.Gray)
    }
}
