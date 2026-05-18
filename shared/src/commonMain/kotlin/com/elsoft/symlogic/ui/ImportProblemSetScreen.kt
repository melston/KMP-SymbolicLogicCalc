package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    val errorColor = MaterialTheme.colors.error
    val successColor = Color.Green

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Problem Set") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                label = { Text("Paste Plain Text Here or Load From File") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(Modifier.height(16.dp))

            FilePickerButton { fileContent ->
                message = null // Clear previous messages
                val trimmedContent = fileContent.trim()
                if (trimmedContent.startsWith("{") && trimmedContent.endsWith("}")) {
                    message = "Incorrect Format: Cannot import JSON files directly. " +
                            "Please provide content in Problem Set format."
                    messageColor = errorColor
                    textContent = "" // Clear the text area
                } else {
                    textContent = fileContent
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (setName.isNotBlank() && textContent.isNotBlank()) {
                        coroutineScope.launch {
                            try {
                                val problemSet = parser.parse(setName, textContent)
                                repository.saveProblemSet(problemSet)
                                message = "Successfully parsed and imported '${problemSet.name}' with ${problemSet.problems.size} problems."
                                messageColor = successColor
                                setName = ""
                                textContent = ""
                            } catch (e: Exception) {
                                message = "Error parsing plain text: ${e.message}"
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
