package logic

/**
 * Represents a problem that has been authored (e.g., from a textbook or file).
 * It does not contain a pre-computed solution, as the solution is meant to be
 * found by the user.
 *
 * @param id A unique identifier for the problem within its set (e.g., "Copi 3.4 #2").
 * @param premises A list of given expressions that are assumed to be true.
 * @param conclusion The target expression that needs to be proven from the premises.
 */
data class AuthoredProblem(
    val id: String,
    val premises: List<Expression>,
    val conclusion: Expression
)

/**
 * A collection of authored problems, typically read from a single file or source.
 *
 * @param name The name of the entire problem set (e.g., "Copi Chapter 3, Section 4").
 * @param problems The list of individual problems in the set.
 */
data class ProblemSet(
    val name: String,
    val problems: List<AuthoredProblem>
)
