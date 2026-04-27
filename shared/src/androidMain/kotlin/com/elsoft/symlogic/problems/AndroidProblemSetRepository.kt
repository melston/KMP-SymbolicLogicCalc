package com.elsoft.symlogic.problems

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Actual implementation for Android
class AndroidProblemSetRepository(private val context: Context) : ProblemSetRepository {

    private val json = Json { prettyPrint = true }
    private val storageDir: File = context.filesDir

    private fun getFileForSet(name: String): File {
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
            ?.mapNotNull { loadProblemSet(it.name.removeSuffix(".json"))?.name }
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

// Actual function to provide the Android repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    val androidContext = context as? Context
        ?: throw IllegalArgumentException("An Android Context is required to get the repository on this platform.")
    return AndroidProblemSetRepository(androidContext)
}
