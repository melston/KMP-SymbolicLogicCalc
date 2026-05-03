package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression

/**
 * Represents the outcome of a proof validation attempt.
 */
sealed class ValidationResult {
    /** The proof is valid and the conclusion has been reached. */
    object Complete : ValidationResult()

    /** The proof is valid so far, but the conclusion has not yet been reached. */
    object ValidSoFar : ValidationResult()

    /** The proof contains a logical or structural error. */
    data class Invalid(val stepId: Int, val reason: String) : ValidationResult()
}

/**
 * Provides logic to verify the mathematical validity of a [Proof].
 */
class ProofValidator {

    fun validate(proof: Proof): ValidationResult {
        val activeExpressions = mutableMapOf<Int, Expression>()
        proof.problem.premises.forEachIndexed { index, expr ->
            activeExpressions[index + 1] = expr
        }

        val scopeStack = mutableListOf(activeExpressions.keys.toMutableSet())
        val activeAssumptionIds = mutableListOf<Int>()
        val seenStepIds = mutableSetOf<Int>()
        seenStepIds.addAll(activeExpressions.keys)

        for ((index, step) in proof.steps.withIndex()) {
            if (seenStepIds.contains(step.id)) {
                return ValidationResult.Invalid(step.id, "Duplicate step ID: ${step.id}")
            }
            seenStepIds.add(step.id)

            when (step) {
                is Proof.ProofStep.RegularStep -> {
                    val parentExpressions = step.parentStepIds.map { parentId ->
                        activeExpressions[parentId] 
                            ?: return ValidationResult.Invalid(step.id, "Parent step '$parentId' is not accessible or does not exist.")
                    }
                    if (!step.rule.validate(step.expression, parentExpressions)) {
                        return ValidationResult.Invalid(step.id, "Invalid application of rule '${step.rule.name}' to parents ${step.parentStepIds}.")
                    }
                    activeExpressions[step.id] = step.expression
                    scopeStack.last().add(step.id)
                }
                is Proof.ProofStep.Assumption -> {
                    val isFirstInBlock = index == 0 || proof.steps.getOrNull(index - 1) !is Proof.ProofStep.Assumption
                    if (isFirstInBlock) {
                        scopeStack.add(mutableSetOf())
                    }
                    activeExpressions[step.id] = step.expression
                    activeAssumptionIds.add(step.id)
                    scopeStack.last().add(step.id)
                }
                is Proof.ProofStep.ImplicationIntroductionStep -> {
                    if (scopeStack.size <= 1) return ValidationResult.Invalid(step.id, "Cannot apply Implication Introduction outside of a sub-proof.")
                    if (step.assumptionIds.isEmpty()) return ValidationResult.Invalid(step.id, "Implication Introduction requires at least one assumption ID.")
                    step.assumptionIds.forEach { assumptionId ->
                        if (assumptionId !in activeAssumptionIds) return ValidationResult.Invalid(step.id, "Assumption '$assumptionId' is not an active assumption.")
                    }
                    val conclusionExpr = activeExpressions[step.conclusionOfSubProofId]
                        ?: return ValidationResult.Invalid(step.id, "Conclusion step '${step.conclusionOfSubProofId}' is not accessible.")
                    
                    val assumptionExpressions = step.assumptionIds.map { activeExpressions[it]!! }
                    val combinedAssumptions = buildConjunction(assumptionExpressions)
                    val expectedExpression = Expression.Implies(combinedAssumptions, conclusionExpr)

                    if (step.expression != expectedExpression) {
                        return ValidationResult.Invalid(step.id, "Resulting expression does not match the expected implication. Expected: $expectedExpression, Found: ${step.expression}")
                    }

                    val closedScopeIds = scopeStack.removeLast()
                    closedScopeIds.forEach { id ->
                        activeExpressions.remove(id)
                        activeAssumptionIds.remove(id)
                    }
                    activeExpressions[step.id] = step.expression
                    scopeStack.last().add(step.id)
                }
            }
        }

        // A proof can only be complete if all sub-proofs are closed.
        if (scopeStack.size > 1) {
            return ValidationResult.ValidSoFar // It's not invalid, just incomplete.
        }

        val lastExpression = proof.steps.lastOrNull()?.expression ?: proof.problem.premises.lastOrNull()
        if (lastExpression == proof.problem.conclusion) {
            return ValidationResult.Complete
        }

        return ValidationResult.ValidSoFar
    }

    private fun buildConjunction(expressions: List<Expression>): Expression {
        if (expressions.isEmpty()) throw IllegalArgumentException("Cannot build conjunction of empty list")
        if (expressions.size == 1) return expressions.first()
        
        var current = expressions[0]
        for (i in 1 until expressions.size) {
            current = Expression.And(current, expressions[i])
        }
        return current
    }
}
