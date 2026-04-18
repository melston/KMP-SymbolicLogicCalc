package com.elsoft.symlogic.problems

// Define the expect interface for the repository
interface ProblemSetRepository {
    suspend fun saveProblemSet(problemSet: ProblemSet, filename: String)
    suspend fun loadProblemSet(filename: String): ProblemSet?
}

// Define the expect function to get the platform-specific repository instance
expect fun getProblemSetRepository(context: Any? = null): ProblemSetRepository
