package com.elsoft.symlogic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.problems.ProblemDefinition
import com.elsoft.symlogic.problems.ProblemSet
import com.elsoft.symlogic.problems.getProblemSetRepository
import com.elsoft.symlogic.problems.parsers.ExpressionParser
import com.elsoft.symlogic.problems.parsers.ProblemSetParser
import com.elsoft.symlogic.ui.components.FilePickerButton
import kotlinx.coroutines.launch

@Composable
fun ProblemSetManagementScreen(onBack: () -> Unit) {
    val repository = remember { getProblemSetRepository() }
    val coroutineScope = rememberCoroutineScope()

    var problemSets by remember { mutableStateOf(emptyList<ProblemSet>()) }
    var selectedSet by remember { mutableStateOf<ProblemSet?>(null) }
    
    var showAddSetDialog by remember { mutableStateOf(false) }
    var showProblemDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var problemToEdit by remember { mutableStateOf<ProblemDefinition?>(null) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }
    var problemToMove by remember { mutableStateOf<ProblemDefinition?>(null) }

    fun refreshProblemSets(selectSetNamed: String? = selectedSet?.name) {
        coroutineScope.launch {
            val names = repository.listProblemSetNames()
            val sets = names.mapNotNull { repository.loadProblemSet(it) }
            problemSets = sets
            if (selectSetNamed != null) {
                selectedSet = sets.find { it.name == selectSetNamed }
            } else {
                selectedSet = null
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshProblemSets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problem Set Management") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Left Pane
            Column(modifier = Modifier.weight(0.4f).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Problem Sets", style = MaterialTheme.typography.h6)
                    Row {
                        IconButton(onClick = { showAddSetDialog = true }) { Icon(Icons.Default.Add, "Add New Set") }
                        IconButton(onClick = { showImportDialog = true }) { Icon(Icons.Default.UploadFile, "Import from File") }
                        IconButton(onClick = { itemToDelete = selectedSet }, enabled = selectedSet != null) { Icon(Icons.Default.Delete, "Delete Set") }
                    }
                }
                Divider()
                LazyColumn {
                    items(problemSets) { set ->
                        Text(
                            text = set.name,
                            modifier = Modifier.fillMaxWidth().clickable { selectedSet = set }.padding(8.dp),
                            fontWeight = if (selectedSet?.name == set.name) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Right Pane
            Column(modifier = Modifier.weight(0.6f).padding(16.dp)) {
                selectedSet?.let { set ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(set.name, style = MaterialTheme.typography.h6)
                        IconButton(onClick = { problemToEdit = null; showProblemDialog = true }) { Icon(Icons.Default.Add, "Add New Problem") }
                    }
                    Divider()
                    LazyColumn {
                        items(set.problems) { problem ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = problem.id,
                                    modifier = Modifier.weight(1f).clickable { problemToEdit = problem; showProblemDialog = true }.padding(8.dp)
                                )
                                if (set.name == "Generated") {
                                    IconButton(onClick = { problemToMove = problem }) { Icon(Icons.Default.Send, "Move Problem") }
                                }
                                IconButton(onClick = { itemToDelete = problem }) { Icon(Icons.Default.Delete, "Delete Problem") }
                            }
                        }
                    }
                } ?: Text("Select a problem set to view its problems.")
            }
        }
    }

    if (showAddSetDialog) {
        AddSetDialog(
            existingSetNames = problemSets.map { it.name },
            onDismiss = { showAddSetDialog = false },
            onAddSet = { newName ->
                coroutineScope.launch {
                    val newSet = ProblemSet(name = newName, problems = emptyList())
                    repository.saveProblemSet(newSet)
                    refreshProblemSets(selectSetNamed = newName)
                    showAddSetDialog = false
                }
            }
        )
    }

    if (showProblemDialog) {
        val currentSet = selectedSet ?: return
        ProblemDialog(
            problem = problemToEdit,
            onDismiss = { showProblemDialog = false },
            onSave = { id, premises, conclusion ->
                coroutineScope.launch {
                    val updatedProblems = selectedSet!!.problems.toMutableList()
                    val existingIndex = updatedProblems.indexOfFirst { it.id == problemToEdit?.id }
                    val newProblem = ProblemDefinition(id, premises, conclusion)

                    if (existingIndex != -1) {
                        updatedProblems[existingIndex] = newProblem
                    } else {
                        updatedProblems.add(newProblem)
                    }
                    
                    val updatedSet = selectedSet!!.copy(problems = updatedProblems)
                    repository.saveProblemSet(updatedSet)
                    refreshProblemSets(selectSetNamed = updatedSet.name)
                    showProblemDialog = false
                }
            }
        )
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            item = item,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                coroutineScope.launch {
                    when (item) {
                        is ProblemSet -> {
                            repository.deleteProblemSet(item.name)
                            refreshProblemSets(selectSetNamed = null)
                        }
                        is ProblemDefinition -> {
                            val currentSet = selectedSet ?: return@launch
                            val updatedProblems = currentSet.problems.filterNot { it.id == item.id }
                            val updatedSet = currentSet.copy(problems = updatedProblems)
                            repository.saveProblemSet(updatedSet)
                            refreshProblemSets(selectSetNamed = updatedSet.name)
                        }
                    }
                    itemToDelete = null
                }
            }
        )
    }

    if (showImportDialog) {
        ImportDialog(
            existingSetNames = problemSets.map { it.name },
            onDismiss = { showImportDialog = false },
            onImport = { name, content ->
                coroutineScope.launch {
                    val problemSet = ProblemSetParser().parse(name, content)
                    repository.saveProblemSet(problemSet)
                    refreshProblemSets(selectSetNamed = name)
                    showImportDialog = false
                }
            }
        )
    }

    problemToMove?.let { problem ->
        MoveProblemDialog(
            problem = problem,
            existingSetNames = problemSets.map { it.name }.filter { it != "Generated" },
            onDismiss = { problemToMove = null },
            onMove = { targetSetName ->
                coroutineScope.launch {
                    repository.moveProblem(problem, "Generated", targetSetName)
                    refreshProblemSets(selectSetNamed = targetSetName)
                    problemToMove = null
                }
            }
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(item: Any, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val name = when (item) {
        is ProblemSet -> "set '${item.name}'"
        is ProblemDefinition -> "problem '${item.id}'"
        else -> "item"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to permanently delete the $name?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddSetDialog(existingSetNames: List<String>, onDismiss: () -> Unit, onAddSet: (String) -> Unit) {
    var newSetName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Problem Set") },
        text = {
            Column {
                OutlinedTextField(
                    value = newSetName,
                    onValueChange = {
                        newSetName = it
                        errorMessage = null
                    },
                    label = { Text("Set Name") },
                    isError = errorMessage != null
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = newSetName.trim()
                    if (trimmedName.isBlank()) {
                        errorMessage = "Name cannot be empty."
                    } else if (existingSetNames.any { it.equals(trimmedName, ignoreCase = true) }) {
                        errorMessage = "A set with this name already exists."
                    } else {
                        onAddSet(trimmedName)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProblemDialog(
    problem: ProblemDefinition?,
    onDismiss: () -> Unit,
    onSave: (id: String, premises: List<Expression>, conclusion: Expression) -> Unit
) {
    var id by remember { mutableStateOf(problem?.id ?: "") }
    var premisesText by remember { mutableStateOf(problem?.premises?.joinToString("\n") ?: "") }
    var conclusionText by remember { mutableStateOf(problem?.conclusion?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val parser = remember { ExpressionParser() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (problem == null) "Add Problem" else "Edit Problem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Problem ID") })
                OutlinedTextField(value = premisesText, onValueChange = { premisesText = it }, label = { Text("Premises (one per line)") }, maxLines = 5)
                OutlinedTextField(value = conclusionText, onValueChange = { conclusionText = it }, label = { Text("Conclusion") })
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val premises = premisesText.lines().filter { it.isNotBlank() }.map { parser.parse(it) }
                    val conclusion = parser.parse(conclusionText)
                    if (id.isBlank() || premises.isEmpty() || conclusionText.isBlank()) {
                        errorMessage = "All fields must be filled."
                    } else {
                        onSave(id, premises, conclusion)
                    }
                } catch (e: Exception) {
                    errorMessage = "Parsing Error: ${e.message}"
                }
            }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MoveProblemDialog(
    problem: ProblemDefinition,
    existingSetNames: List<String>,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit
) {
    var newSetName by remember { mutableStateOf("") }
    var selectedExistingSet by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move Problem '${problem.id}'") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select an existing set or create a new one.")
                
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(existingSetNames) { name ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedExistingSet = name
                                newSetName = ""
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedExistingSet == name,
                                onClick = { 
                                    selectedExistingSet = name
                                    newSetName = ""
                                }
                            )
                            Text(name)
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = newSetName,
                    onValueChange = {
                        newSetName = it
                        selectedExistingSet = ""
                        errorMessage = null
                    },
                    label = { Text("New Set Name") },
                    isError = errorMessage != null
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val target = newSetName.trim().takeIf { it.isNotEmpty() } ?: selectedExistingSet
                if (target.isBlank()) {
                    errorMessage = "Please select or create a set."
                } else {
                    onMove(target)
                }
            }) { Text("Move") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ImportDialog(
    existingSetNames: List<String>,
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    var setName by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Problem Set from File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = setName,
                    onValueChange = {
                        setName = it
                        errorMessage = null
                    },
                    label = { Text("New Problem Set Name") },
                    isError = errorMessage != null
                )
                
                FilePickerButton { fileContent ->
                    errorMessage = null
                    val trimmedContent = fileContent.trim()
                    if (trimmedContent.startsWith("{") && trimmedContent.endsWith("}")) {
                        errorMessage = "Incorrect Format: Cannot import JSON files directly."
                        textContent = ""
                    } else {
                        textContent = fileContent
                    }
                }

                OutlinedTextField(
                    value = textContent,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("File Content Preview") },
                    modifier = Modifier.height(150.dp).fillMaxWidth()
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmedName = setName.trim()
                if (trimmedName.isBlank()) {
                    errorMessage = "Set name cannot be empty."
                } else if (existingSetNames.any { it.equals(trimmedName, ignoreCase = true) }) {
                    errorMessage = "A set with this name already exists."
                } else if (textContent.isBlank()) {
                    errorMessage = "Please load a file."
                } else {
                    onImport(trimmedName, textContent)
                }
            }) { Text("Import and Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
