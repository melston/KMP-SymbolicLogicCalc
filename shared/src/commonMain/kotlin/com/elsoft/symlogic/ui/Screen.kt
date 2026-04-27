package com.elsoft.symlogic.ui

import com.elsoft.symlogic.problems.ProblemDefinition

/**
 * Defines the different screens available in the application.
 * Using a sealed class allows screens to carry data (e.g., the problem for the solver screen).
 */
sealed class Screen {
    data object MainMenu : Screen()
    data object GeneratedProblems : Screen()
    data object PreWrittenProblems : Screen()
    data object ImportProblemSet : Screen()
    data class Solver(val problem: ProblemDefinition) : Screen()
}
