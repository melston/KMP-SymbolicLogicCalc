package com.elsoft.symlogic.ui.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

class JvmTextExporter : TextExporter {
    override fun exportText(filename: String, content: String) {
        val dialog = FileDialog(null as Frame?, "Export Proof As...", FileDialog.SAVE)
        dialog.file = filename // Suggest a filename
        dialog.isVisible = true

        if (dialog.file != null) {
            val file = File(dialog.directory, dialog.file)
            file.writeText(content)
        }
    }
}

actual fun getTextExporter(context: Any?): TextExporter = JvmTextExporter()
