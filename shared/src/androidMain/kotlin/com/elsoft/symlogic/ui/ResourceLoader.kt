package com.elsoft.symlogic.ui

import android.content.Context

// In a real app, you would get the context from a DI framework or pass it down.
// For now, we'll assume a global context is available for simplicity.
// This is a common pattern in KMP for platform-specific needs.
lateinit var AppContext: Context

actual suspend fun loadHelpResource(path: String): String {
    return try {
        AppContext.assets.open(path).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error loading resource: ${e.message}"
    }
}
