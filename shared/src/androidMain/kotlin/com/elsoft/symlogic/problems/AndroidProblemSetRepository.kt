package com.elsoft.symlogic.problems

import android.content.Context
import kotlinx.serialization.encodeToString
import java.io.File

// Actual implementation for Android
class AndroidProblemSetRepository(private val context: Context) : ProblemSetRepository {

    private val json = AppJson // Use the globally configured Json instance
    private val baseDir: File
    private val setsDir: File
    private val proofsDir: File

    init {
        baseDir = context.filesDir
        setsDir = File(baseDir, "problem_sets")
        proofsDir = File(baseDir, "proofs")
        if (!setsDir.exists()) setsDir.mkdirs()
        if (!proofsDir.exists()) proofsDir.mkdirs()
    }

    private fun getFileForSet(name: String): File {
        val filename = name.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".json"
        return File(setsDir, filename)
    }

    private fun getDirForProof(setName: String): File {
        val dirName = setName.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return File(proofsDir, dirName)
    }

    private fun getFileForProof(setName: String, problemId: String): File {
        val dir = getDirForProof(setName)
        if (!dir.exists()) dir.mkdirs()
        val filename = problemId.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".json"
        return File(dir, filename)
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
        return setsDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull {
                try {
                    json.decodeFromString<ProblemSet>(it.readText()).name
                } catch (e: Exception) {
                    null
                }
            }
            ?: emptyList()
    }

    override suspend fun deleteProblemSet(name: String): Boolean {
        val file = getFileForSet(name)
        return if (file.exists()) file.delete() else false
    }

    override suspend fun saveProof(setName: String, proof: Proof) {
        val file = getFileForProof(setName, proof.problem.id)
        val jsonString = json.encodeToString(proof)
        file.writeText(jsonString)
    }

    override suspend fun loadProof(setName: String, problem: ProblemDefinition): Proof? {
        val file = getFileForProof(setName, problem.id)
        if (!file.exists()) return null
        return try {
            val jsonString = file.readText()
            json.decodeFromString<Proof>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun listSolvedProblemIds(setName: String): Set<String> {
        val dir = getDirForProof(setName)
        if (!dir.exists()) return emptySet()
        return dir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull {
                try {
                    json.decodeFromString<Proof>(it.readText()).problem.id
                } catch (e: Exception) {
                    null
                }
            }
            ?.toSet() ?: emptySet()
    }
}

// Actual function to provide the Android repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    val androidContext = context as? Context
        ?: throw IllegalArgumentException("An Android Context is required to get the repository on this platform.")
    return AndroidProblemSetRepository(androidContext)
}
