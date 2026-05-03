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
     * Saves a user's proof. The proof is associated with its problem definition.
     */
    suspend fun saveProof(proof: Proof)

    /**
     * Loads a saved proof for a given problem.
     * @param problem The definition of the problem for which to load the proof.
     */
    suspend fun loadProof(problem: ProblemDefinition): Proof?

    /**
     * Lists the IDs of all problems that have a saved proof within a given problem set.
     * @param setName The name of the problem set.
     */
    suspend fun listSolvedProblemIds(setName: String): Set<String>
}

/**
 * Gets the platform-specific instance of the ProblemSetRepository.
 * @param context An optional platform-specific context (e.g., Android Context).
 */
expect fun getProblemSetRepository(context: Any? = null): ProblemSetRepository
