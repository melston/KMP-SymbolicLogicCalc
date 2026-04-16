package logic

// Defines exactly which premises led to a derived expression
data class Derivation(
    val result: Expression,
    val rule: Rule,
    val parents: List<Expression>
)

interface Rule {
    val name: String
    
    /**
     * Attempts to apply this rule to the given expressions.
     * Returns a list of new derivations that can be made.
     */
    fun apply(expressions: List<Expression>): List<Derivation>
}
