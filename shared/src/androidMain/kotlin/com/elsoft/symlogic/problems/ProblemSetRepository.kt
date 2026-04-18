package com.elsoft.symlogic.problems

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Actual implementation for Android
class AndroidProblemSetRepository(private val context: Context) : ProblemSetRepository {

    private val json = Json { prettyPrint = true }
    private val filesDir: File = context.filesDir // Get the app's internal files directory

    override suspend fun saveProblemSet(problemSet: ProblemSet, filename: String) {
        val jsonString = json.encodeToString(problemSet)
        val file = File(filesDir, filename) // Save to app's internal storage
        file.writeText(jsonString)
        println("ProblemSet '${problemSet.name}' saved to ${file.absolutePath}")
    }

    override suspend fun loadProblemSet(filename: String): ProblemSet? {
        val file = File(filesDir, filename) // Load from app's internal storage
        if (!file.exists()) {
            println("File not found: ${file.absolutePath}")
            return null
        }
        return try {
            val jsonString = file.readText()
            json.decodeFromString<ProblemSet>(jsonString)
        } catch (e: Exception) {
            println("Error loading ProblemSet from ${file.absolutePath}: ${e.message}")
            null
        }
    }
}

// Actual function to provide the Android repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    // Cast the Any? context to an Android Context
    val androidContext = context as? Context
        ?: throw IllegalArgumentException("Context must be an Android Context for Android target.")
    return AndroidProblemSetRepository(androidContext)
}
