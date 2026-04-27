package com.elsoft.symlogic.problems

/**
 * An interface for a repository that manages the storage of ProblemSets.
 * This abstracts away the platform-specific details of file handling.
 */
interface ProblemSetRepository {
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
}

/**
 * Gets the platform-specific instance of the ProblemSetRepository.
 * @param context An optional platform-specific context (e.g., Android Context).
 */
expect fun getProblemSetRepository(context: Any? = null): ProblemSetRepository
