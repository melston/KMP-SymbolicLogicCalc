package com.elsoft.symlogic.problems

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Actual implementation for JVM
class JvmProblemSetRepository : ProblemSetRepository {

    private val json = Json { prettyPrint = true }
    private val storageDir: File

    init {
        val userHome = System.getProperty("user.home")
        storageDir = File(userHome, ".symlogic/problems")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    private fun getFileForSet(name: String): File {
        // Create a safe filename from the problem set name
        val filename = name.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".json"
        return File(storageDir, filename)
    }

    override suspend fun saveProblemSet(problemSet: ProblemSet) {
        val file = getFileForSet(problemSet.name)
        val jsonString = json.encodeToString(problemSet)
        file.writeText(jsonString)
    }

    override suspend fun loadProblemSet(name: String): ProblemSet? {
        val file = getFileForSet(name)
        if (!file.exists()) return null
        return try {
            val jsonString = file.readText()
            json.decodeFromString<ProblemSet>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun listProblemSetNames(): List<String> {
        return storageDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { loadProblemSet(it.name.removeSuffix(".json"))?.name } // Load to get the original name
            ?: emptyList()
    }

    override suspend fun deleteProblemSet(name: String): Boolean {
        val file = getFileForSet(name)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}

// Actual function to provide the JVM repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    return JvmProblemSetRepository()
}
