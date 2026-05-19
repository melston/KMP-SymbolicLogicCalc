package com.elsoft.symlogic.ui.util

/**
 * An interface for exporting text content, abstracting platform-specific implementations.
 */
interface TextExporter {
    /**
     * Exports the given text content.
     * On desktop, this will typically open a "Save As" dialog.
     * On Android, this will typically open a system "Share" sheet.
     *
     * @param filename A suggested filename for the content (used primarily on desktop).
     * @param content The text content to be exported/shared.
     */
    fun exportText(filename: String, content: String)
}

/**
 * Gets the platform-specific instance of the TextExporter.
 * @param context An optional platform-specific context (e.g., Android Context).
 */
expect fun getTextExporter(context: Any? = null): TextExporter
