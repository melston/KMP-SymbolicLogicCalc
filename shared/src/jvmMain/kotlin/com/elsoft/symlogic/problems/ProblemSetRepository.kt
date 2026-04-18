package com.elsoft.symlogic.problems

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Actual implementation for JVM
class JvmProblemSetRepository : ProblemSetRepository {

    private val json = Json { prettyPrint = true }

    override suspend fun saveProblemSet(problemSet: ProblemSet, filename: String) {
        val jsonString = json.encodeToString(problemSet)
        val file = File(filename)
        file.writeText(jsonString)
        println("ProblemSet '${problemSet.name}' saved to ${file.absolutePath}")
    }

    override suspend fun loadProblemSet(filename: String): ProblemSet? {
        val file = File(filename)
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

// Actual function to provide the JVM repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    // Context is not needed for JVM file operations, so it's ignored.
    return JvmProblemSetRepository()
}
