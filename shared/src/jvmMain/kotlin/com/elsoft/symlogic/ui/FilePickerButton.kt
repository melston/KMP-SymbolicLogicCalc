package com.elsoft.symlogic.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun FilePickerButton(onFilePicked: (String) -> Unit) {
    Button(onClick = {
        val dialog = FileDialog(null as Frame?, "Select Problem Set File", FileDialog.LOAD)
        dialog.isVisible = true
        
        if (dialog.file != null) {
            val file = File(dialog.directory, dialog.file)
            onFilePicked(file.readText())
        }
    }) {
        Text("Load From File...")
    }
}
