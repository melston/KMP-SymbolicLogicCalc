package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.problems.getProblemSetRepository
import com.elsoft.symlogic.problems.parsers.ProblemSetParser
import kotlinx.coroutines.launch

@Composable
fun ImportProblemSetScreen(onBack: () -> Unit) {
    val repository = remember { getProblemSetRepository() }
    val parser = remember { ProblemSetParser() }
    val coroutineScope = rememberCoroutineScope()

    var textContent by remember { mutableStateOf("") }
    var setName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var messageColor by remember { mutableStateOf(Color.Green) }

    // Read theme colors once in the Composable body
    val errorColor = MaterialTheme.colors.error
    val successColor = Color.Green // Or a color from your theme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Problem Set") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = setName,
                onValueChange = { setName = it },
                label = { Text("Problem Set Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                label = { Text("Paste Problem Set Text Here") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(Modifier.height(16.dp))

            // Platform-specific button to load from a file
            FilePickerButton { fileContent ->
                textContent = fileContent
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (setName.isNotBlank() && textContent.isNotBlank()) {
                        coroutineScope.launch {
                            try {
                                val problemSet = parser.parse(setName, textContent)
                                repository.saveProblemSet(problemSet)
                                message = "Successfully imported '${problemSet.name}' with ${problemSet.problems.size} problems."
                                messageColor = successColor
                                // Clear fields on success
                                setName = ""
                                textContent = ""
                            } catch (e: Exception) {
                                message = "Error: ${e.message}"
                                messageColor = errorColor
                            }
                        }
                    } else {
                        message = "Please provide a name and content for the problem set."
                        messageColor = errorColor
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import and Save")
            }

            message?.let {
                Text(
                    text = it,
                    color = messageColor,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/**
 * A platform-specific button that opens a file picker and returns the file content.
 */
@Composable
expect fun FilePickerButton(onFilePicked: (String) -> Unit)
