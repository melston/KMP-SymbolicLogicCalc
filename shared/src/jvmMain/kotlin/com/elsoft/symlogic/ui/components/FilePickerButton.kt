package com.elsoft.symlogic.ui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun FilePickerButton(onFilePicked: (String) -> Unit) {
    Button(onClick = {
        val dialog = FileDialog(null as Frame?, "Select File to Open")
        dialog.mode = FileDialog.LOAD
        dialog.isVisible = true
        val file = dialog.file
        val directory = dialog.directory
        if (file != null && directory != null) {
            val fullPath = File(directory, file)
            onFilePicked(fullPath.readText())
        }
    }) {
        Text("Load from File")
    }
}
