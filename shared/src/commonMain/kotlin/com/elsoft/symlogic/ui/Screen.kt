package com.elsoft.symlogic.ui

import com.elsoft.symlogic.problems.Proof
import com.elsoft.symlogic.problems.ProblemDefinition

/**
 * Defines the different screens available in the application.
 */
sealed class Screen {
    data object MainMenu : Screen()
    data object GeneratedProblems : Screen()
    data object PreWrittenProblems : Screen()
    data object ImportProblemSet : Screen()

    /**
     * The solver screen.
     * @param proof The initial proof state (can be new or loaded).
     * @param setName The name of the problem set this proof belongs to. Can be null for generated problems.
     */
    data class Solver(val proof: Proof, val setName: String?) : Screen()
}
