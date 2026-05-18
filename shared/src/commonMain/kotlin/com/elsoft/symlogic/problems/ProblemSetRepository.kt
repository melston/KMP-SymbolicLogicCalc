package com.elsoft.symlogic.problems

/**
 * An interface for a repository that manages the storage of ProblemSets and user Proofs.
 * This abstracts away the platform-specific details of file handling.
 */
interface ProblemSetRepository {
    // --- Problem Set Management ---

    /**
     * Saves a ProblemSet. The implementation will determine the storage location
     * based on the platform and the problemSet's name.
     */
    suspend fun saveProblemSet(problemSet: ProblemSet)

    /**
     * Loads a ProblemSet by its unique name.
     */
    suspend fun loadProblemSet(name: String): ProblemSet?

    /**
     * Lists the names of all currently stored ProblemSets.
     */
    suspend fun listProblemSetNames(): List<String>

    /**
     * Deletes a ProblemSet by its unique name.
     */
    suspend fun deleteProblemSet(name: String): Boolean

    // --- Proof Management ---

    /**
     * Saves a user's proof, explicitly associating it with a problem set.
     * @param setName The name of the problem set this proof belongs to.
     * @param proof The proof object to save.
     */
    suspend fun saveProof(setName: String, proof: Proof)
    suspend fun loadProof(setName: String, problemId: String): Proof?
    suspend fun deleteProof(setName: String, problemId: String): Boolean
    suspend fun deleteAllProofsInSet(setName: String)

    /**
     * Moves a ProblemDefinition and its associated Proof from one ProblemSet to another.
     * This involves updating both ProblemSet files and moving the Proof file.
     * @param problem The ProblemDefinition to move.
     * @param sourceSetName The name of the ProblemSet the problem is currently in.
     * @param targetSetName The name of the ProblemSet to move the problem to.
     */
    suspend fun moveProblem(problem: ProblemDefinition, sourceSetName: String, targetSetName: String)

    /**
     * Lists the status of all problems that have a saved proof within a given problem set.
     * @param setName The name of the problem set.
     * @return A map of Problem ID to its ProofStatus.
     */
    suspend fun getProofStatuses(setName: String): Map<String, ProofStatus>
}

/**
 * Gets the platform-specific instance of the ProblemSetRepository.
 * @param context An optional platform-specific context (e.g., Android Context).
 */
expect fun getProblemSetRepository(context: Any? = null): ProblemSetRepository
