package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val stepId: Int, val reason: String) : ValidationResult()
}

/**
 * Provides logic to verify the mathematical validity of a [Proof].
 * It ensures that every derivation follows the rules of logic,
 * that scopes (sub-proofs) are correctly managed, and that the
 * final expression matches the intended conclusion.
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
                            ?: return ValidationResult.Invalid(step.id, "Parent step '$parentId' is not accessible or does not exist. Did you reference a step from a closed sub-proof?")
                    }

                    if (!step.rule.validate(step.expression, parentExpressions)) {
                        return ValidationResult.Invalid(step.id, "Invalid application of rule '${step.rule.name}' to parents ${step.parentStepIds}.")
                    }

                    activeExpressions[step.id] = step.expression
                    scopeStack.last().add(step.id)
                }

                is Proof.ProofStep.Assumption -> {
                    // If the previous step was NOT an assumption, start a new sub-proof scope.
                    val isFirstInBlock = index == 0 || proof.steps.getOrNull(index - 1) !is Proof.ProofStep.Assumption
                    if (isFirstInBlock) {
                        scopeStack.add(mutableSetOf())
                    }
                    
                    activeExpressions[step.id] = step.expression
                    activeAssumptionIds.add(step.id)
                    scopeStack.last().add(step.id)
                }

                is Proof.ProofStep.ImplicationIntroductionStep -> {
                    if (scopeStack.size <= 1) {
                        return ValidationResult.Invalid(step.id, "Cannot apply Implication Introduction outside of a sub-proof.")
                    }
                    if (step.assumptionIds.isEmpty()) {
                        return ValidationResult.Invalid(step.id, "Implication Introduction requires at least one assumption ID.")
                    }
                    step.assumptionIds.forEach { assumptionId ->
                        if (assumptionId !in activeAssumptionIds) {
                            return ValidationResult.Invalid(step.id, "Assumption '$assumptionId' is not an active assumption in the current sub-proof.")
                        }
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

        if (scopeStack.size > 1) {
            return ValidationResult.Invalid(0, "Proof contains unclosed sub-proofs.")
        }

        if (proof.steps.isEmpty() && !proof.problem.premises.contains(proof.problem.conclusion)) {
            return ValidationResult.Invalid(0, "Proof is empty and conclusion is not in premises.")
        }
        
        if (proof.steps.isNotEmpty() && proof.steps.last().expression != proof.problem.conclusion) {
            return ValidationResult.Invalid(proof.steps.last().id, "Final step expression (${proof.steps.last().expression}) does not match problem conclusion (${proof.problem.conclusion}).")
        }

        return ValidationResult.Valid
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
