package com.elsoft.symlogic.problems

import kotlinx.serialization.encodeToString
import java.io.File

class JvmProblemSetRepository : ProblemSetRepository {

    private val json = AppJson
    private val baseDir: File
    private val setsDir: File
    private val proofsDir: File
    private val validator = ProofValidator()

    init {
        val userHome = System.getProperty("user.home")
        baseDir = File(userHome, ".symlogic")
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
        file.writeText(json.encodeToString(problemSet))
    }

    override suspend fun loadProblemSet(name: String): ProblemSet? {
        val file = getFileForSet(name)
        if (!file.exists()) return null
        return try { json.decodeFromString<ProblemSet>(file.readText()) } catch (e: Exception) { null }
    }

    override suspend fun listProblemSetNames(): List<String> {
        return setsDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { try { json.decodeFromString<ProblemSet>(it.readText()).name } catch (e: Exception) { null } }
            ?: emptyList()
    }

    override suspend fun deleteProblemSet(name: String): Boolean {
        deleteAllProofsInSet(name)
        val file = getFileForSet(name)
        return if (file.exists()) file.delete() else false
    }

    override suspend fun saveProof(setName: String, proof: Proof) {
        val file = getFileForProof(setName, proof.problem.id)
        file.writeText(json.encodeToString(proof))

        // If this is a generated problem, ensure its ProblemDefinition is also saved in a "Generated" ProblemSet file
        if (setName == "Generated") {
            val generatedSet = loadProblemSet("Generated") ?: ProblemSet("Generated", emptyList())
            val updatedProblems = (generatedSet.problems.filterNot { it.id == proof.problem.id } + proof.problem).distinctBy { it.id }
            saveProblemSet(generatedSet.copy(problems = updatedProblems))
        }
    }

    override suspend fun loadProof(setName: String, problemId: String): Proof? {
        val file = getFileForProof(setName, problemId)
        if (!file.exists()) return null
        return try { json.decodeFromString<Proof>(file.readText()) } catch (e: Exception) { null }
    }

    override suspend fun deleteProof(setName: String, problemId: String): Boolean {
        val file = getFileForProof(setName, problemId)
        return if (file.exists()) file.delete() else false
    }

    override suspend fun deleteAllProofsInSet(setName: String) {
        getDirForProof(setName).deleteRecursively()
        // Also delete the ProblemSet file itself if it's a generated one
        if (setName == "Generated") {
            getFileForSet(setName).delete()
        }
    }

    override suspend fun moveProblem(problem: ProblemDefinition, sourceSetName: String, targetSetName: String) {
        // 1. Move the proof file, if it exists
        val proof = loadProof(sourceSetName, problem.id)
        if (proof != null) {
            saveProof(targetSetName, proof) // This will also update the target ProblemSet if it's "Generated"
            deleteProof(sourceSetName, problem.id)
        }

        // 2. Update the source problem set (remove the problem)
        val sourceSet = loadProblemSet(sourceSetName)
        if (sourceSet != null) {
            val updatedSourceProblems = sourceSet.problems.filterNot { it.id == problem.id }
            saveProblemSet(sourceSet.copy(problems = updatedSourceProblems))
        }

        // 3. Update the target problem set (add the problem)
        val targetSet = loadProblemSet(targetSetName) ?: ProblemSet(targetSetName, emptyList())
        val updatedTargetProblems = (targetSet.problems.filterNot { it.id == problem.id } + problem).distinctBy { it.id }
        saveProblemSet(targetSet.copy(problems = updatedTargetProblems))
    }

    override suspend fun getProofStatuses(setName: String): Map<String, ProofStatus> {
        val dir = getDirForProof(setName)
        if (!dir.exists()) return emptyMap()
        
        return dir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val proof = json.decodeFromString<Proof>(file.readText())
                    val status = when (validator.validate(proof)) {
                        is ValidationResult.Complete -> ProofStatus.Completed
                        else -> ProofStatus.InProgress
                    }
                    proof.problem.id to status
                } catch (e: Exception) {
                    null
                }
            }
            ?.toMap() ?: emptyMap()
    }
}

actual fun getProblemSetRepository(context: Any?): ProblemSetRepository {
    return JvmProblemSetRepository()
}
