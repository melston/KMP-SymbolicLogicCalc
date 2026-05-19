package com.elsoft.symlogic.ui.util

import android.content.Context
import android.content.Intent

class AndroidTextExporter(private val context: Context) : TextExporter {
    override fun exportText(filename: String, content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "Proof: $filename") // Subject for emails, etc.
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        // We need to add this flag to start an activity from a non-activity context
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}

actual fun getTextExporter(context: Any?): TextExporter {
    val androidContext = context as? Context
        ?: throw IllegalArgumentException("An Android Context is required for the TextExporter on this platform.")
    return AndroidTextExporter(androidContext)
}
