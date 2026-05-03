package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Actual implementation for JVM
class JvmProblemSetRepository : ProblemSetRepository {

    private val json = Json { prettyPrint = true }
    private val baseDir: File
    private val setsDir: File
    private val proofsDir: File

    init {
        val userHome = System.getProperty("user.home")
        baseDir = File(userHome, ".symlogic")
        setsDir = File(baseDir, "problems")
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

    private fun getFileForProof(proof: Proof): File {
        val dir = getDirForProof(proof.problem.id.substringBefore(" ")) // Assuming ID format "SetName 1.2"
        if (!dir.exists()) dir.mkdirs()
        val filename = proof.problem.id.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".json"
        return File(dir, filename)
    }
    
    private fun getFileForProof(problem: ProblemDefinition): File {
        val dir = getDirForProof(problem.id.substringBefore(" "))
        val filename = problem.id.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".json"
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
                    null // Ignore malformed files
                }
            }
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

    override suspend fun saveProof(proof: Proof) {
        val file = getFileForProof(proof)
        val jsonString = json.encodeToString(proof)
        file.writeText(jsonString)
    }

    override suspend fun loadProof(problem: ProblemDefinition): Proof? {
        val file = getFileForProof(problem)
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
            ?.map { it.nameWithoutExtension.replace("_", " ") }
            ?.toSet() ?: emptySet()
    }
}

// Actual function to provide the JVM repository instance
actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    return JvmProblemSetRepository()
}
