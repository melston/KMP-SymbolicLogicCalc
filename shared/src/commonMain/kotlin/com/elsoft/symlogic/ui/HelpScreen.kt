package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m2.Markdown

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var markdownContent by remember { mutableStateOf("Loading help...") }

    LaunchedEffect(Unit) {
        markdownContent = try {
            val rawContent = loadHelpResource("help/rules.md")
            // Normalize line endings to handle CRLF from Windows files
            rawContent.replace("\r\n", "\n").replace('\r', '\n')
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: Could not load help file."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help - Rules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Markdown(markdownContent)
        }
    }
}

/**
 * A platform-specific function to load a text resource from the classpath.
 */
expect suspend fun loadHelpResource(path: String): String
