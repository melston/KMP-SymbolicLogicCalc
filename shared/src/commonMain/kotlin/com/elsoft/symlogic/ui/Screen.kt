package com.elsoft.symlogic.ui

import com.elsoft.symlogic.problems.Proof

/**
 * Defines the different screens available in the application.
 */
sealed class Screen {
    data object MainMenu : Screen()
    data object GeneratedProblems : Screen()
    data object PreWrittenProblems : Screen()
    data object ProblemSetManagement : Screen()
    data object Help : Screen() // New Help Screen
    data class Solver(val proof: Proof, val setName: String?) : Screen()
}
