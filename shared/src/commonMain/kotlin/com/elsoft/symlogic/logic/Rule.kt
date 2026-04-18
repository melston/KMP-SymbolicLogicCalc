package com.elsoft.symlogic.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// Defines exactly which premises led to a derived expression
data class Derivation(
    val result: Expression,
    val rule: Rule,
    val parents: List<Expression>
)

@Serializable
sealed interface Rule { // Changed to sealed interface and made Serializable
    val name: String
    
    /**
     * Attempts to apply this rule to the given expressions.
     * Returns a list of new derivations that can be made.
     */
    fun apply(expressions: List<Expression>): List<Derivation>

    /**
     * Validates if the derivedExpression can be correctly obtained from the parentExpressions
     * by applying this rule. This is used by the ProofValidator.
     *
     * @param derivedExpression The expression claimed to be derived.
     * @param parentExpressions The expressions used as input for the rule.
     * @return True if the application is valid, false otherwise.
     */
    fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean
}
