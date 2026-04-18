package com.elsoft.symlogic.logic

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val stepId: String, val reason: String) : ValidationResult()
}

class ProofValidator {

    /**
     * Validates a complete proof.
     * 
     * A proof is valid if:
     * 1. Every step correctly applies a rule to valid parent steps.
     * 2. Every referenced parent step exists and is accessible in the current scope.
     * 3. Sub-proofs are correctly opened with Assumptions and closed with Implication Introduction.
     * 4. No steps reference expressions from closed sub-proofs.
     * 5. The final step's expression matches the problem's conclusion.
     * 6. All sub-proofs have been closed.
     */
    fun validate(proof: Proof): ValidationResult {
        // Map to keep track of accessible expressions.
        // It maps a step ID to the Expression derived at that step.
        val activeExpressions = mutableMapOf<String, Expression>()
        
        // Add premises to the accessible expressions.
        // We will assign them implicit IDs like "P1", "P2", etc.
        proof.problem.premises.forEachIndexed { index, expr ->
            activeExpressions["P${index + 1}"] = expr
        }

        // Stack to track active sub-proof scopes.
        // Each entry in the stack is a set of step IDs that were introduced in that specific scope.
        // The base scope (index 0) holds the main proof steps (including initial premises).
        val scopeStack = mutableListOf(mutableSetOf<String>())
        // Add initial premises to the base scope so they aren't lost
        proof.problem.premises.forEachIndexed { index, _ ->
            scopeStack[0].add("P${index + 1}")
        }
        
        // Track the current active assumptions
        val activeAssumptionIds = mutableListOf<String>()

        val seenStepIds = mutableSetOf<String>()

        // Flag to track if the previous step was an Assumption, used to manage sub-proof scope opening.
        var lastStepWasAssumption = false

        // Implicitly consider premises as steps we've "seen" to prevent ID collisions
        proof.problem.premises.forEachIndexed { index, _ ->
            seenStepIds.add("P${index + 1}")
        }

        for (step in proof.steps) {
            if (seenStepIds.contains(step.id)) {
                return ValidationResult.Invalid(step.id, "Duplicate step ID: ${step.id}")
            }
            seenStepIds.add(step.id)

            when (step) {

                is Proof.ProofStep.RegularStep -> {
                    // 1. Verify parent steps exist and are accessible in the CURRENT scope
                    val parentExpressions = mutableListOf<Expression>()
                    for (parentId in step.parentStepIds) {
                        val parentExpr = activeExpressions[parentId] 
                        if (parentExpr == null) {
                             return ValidationResult.Invalid(step.id, "Parent step '$parentId' is not accessible or does not exist. Did you reference a step from a closed sub-proof?")
                        }
                        parentExpressions.add(parentExpr)
                    }

                    // 2. Validate the rule application
                    if (!step.rule.validate(step.expression, parentExpressions)) {
                        return ValidationResult.Invalid(
                            step.id, 
                            "Invalid application of rule '${step.rule.name}' to parents ${step.parentStepIds}."
                        )
                    }

                    // 3. Add to active expressions and current scope, and reset assumption flag
                    activeExpressions[step.id] = step.expression
                    scopeStack.last().add(step.id)
                    lastStepWasAssumption = false
                }

                is Proof.ProofStep.Assumption -> {
                    // If this is the first assumption of a new sub-proof block, push a new scope.
                    // Otherwise, add to the existing innermost scope.
                    if (!lastStepWasAssumption) {
                        scopeStack.add(mutableSetOf())
                    }
                    // Add the assumption to active expressions, active assumptions, and the current (innermost) scope.
                    activeExpressions[step.id] = step.expression
                    activeAssumptionIds.add(step.id)
                    scopeStack.last().add(step.id)
                    lastStepWasAssumption = true
                }

                is Proof.ProofStep.ImplicationIntroductionStep -> {
                    // 1. Ensure we are actually in a sub-proof
                    if (scopeStack.size <= 1) {
                        return ValidationResult.Invalid(step.id, "Cannot apply Implication Introduction outside of a sub-proof.")
                    }

                    // 2. Verify all specified assumptions are currently active
                    if (step.assumptionIds.isEmpty()) {
                        return ValidationResult.Invalid(step.id, "Implication Introduction requires at least one assumption ID.")
                    }
                    for (assumptionId in step.assumptionIds) {
                        if (!activeAssumptionIds.contains(assumptionId)) {
                            return ValidationResult.Invalid(step.id, "Assumption '$assumptionId' is not an active assumption.")
                        }
                    }

                    // 3. Verify the conclusion step exists and is accessible in the current scope
                    val conclusionExpr = activeExpressions[step.conclusionOfSubProofId]
                    if (conclusionExpr == null) {
                         return ValidationResult.Invalid(step.id, "Conclusion step '${step.conclusionOfSubProofId}' is not accessible.")
                    }

                    // 4. Verify the constructed implication expression is correct
                    // We need to build the conjunction: ((A1 & A2) & A3) ...
                    val assumptionExpressions = step.assumptionIds.map { activeExpressions[it]!! }
                    val combinedAssumptions = buildConjunction(assumptionExpressions)
                    val expectedExpression = Expression.Implies(combinedAssumptions, conclusionExpr)

                    if (step.expression != expectedExpression) {
                        return ValidationResult.Invalid(
                            step.id, 
                            "Resulting expression does not match the expected implication. Expected: $expectedExpression, Found: ${step.expression}"
                        )
                    }

                    // 5. Close the sub-proof: Pop the scope and remove its derived expressions from availability
                    val closedScopeIds = scopeStack.removeLast()
                    for (id in closedScopeIds) {
                        activeExpressions.remove(id)
                    }
                    // Also remove the discharged assumptions from the active tracking list
                    activeAssumptionIds.removeAll(step.assumptionIds)

                    // 6. The Implication Introduction step itself belongs to the *parent* scope
                    //    because it represents the result of the entire sub-proof block.
                    activeExpressions[step.id] = step.expression
                    // Add to the parent scope (which is now the last one after popping)
                    scopeStack.last().add(step.id)
                    lastStepWasAssumption = false // Reset flag
                }
            }
        }

        // Final checks after processing all steps
        if (scopeStack.size > 1) {
            return ValidationResult.Invalid("EOF", "Proof contains unclosed sub-proofs.")
        }

        if (proof.steps.isEmpty()) {
            return ValidationResult.Invalid("EOF", "Proof is empty.")
        }

        val lastStep = proof.steps.last()
        if (lastStep.expression != proof.problem.conclusion) {
            return ValidationResult.Invalid(
                lastStep.id, 
                "Final step expression (${lastStep.expression}) does not match problem conclusion (${proof.problem.conclusion})."
            )
        }

        return ValidationResult.Valid
    }

    /**
     * Helper to build a left-associative conjunction from a list of expressions.
     * e.g., [A, B, C] -> ((A & B) & C)
     */
    private fun buildConjunction(expressions: List<Expression>): Expression {
        require(expressions.isNotEmpty()) { "Cannot build conjunction of empty list" }
        if (expressions.size == 1) return expressions.first()
        
        var current = expressions[0]
        for (i in 1 until expressions.size) {
            current = Expression.And(current, expressions[i])
        }
        return current
    }
}
