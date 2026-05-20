package com.elsoft.symlogic.ui

actual suspend fun loadHelpResource(path: String): String {
    return try {
        val classLoader = Thread.currentThread().contextClassLoader!!
        classLoader.getResourceAsStream(path)?.bufferedReader()?.readText()
            ?: "Error: Resource not found at path '$path'"
    } catch (e: Exception) {
        e.printStackTrace()
        "Error loading resource: ${e.message}"
    }
}
