package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.logic.Rule
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ProblemDefinition(
    val id: String,
    val premises: List<Expression>,
    val conclusion: Expression
)

@Serializable
data class Proof(
    val problem: ProblemDefinition,
    val steps: List<ProofStep> = emptyList()
) {
    fun addAssumption(wff: Expression): Proof {
        val assumption = ProofStep.Assumption(getNextStepId(), wff)
        return withNewStep(assumption)
    }

    fun addStep(wff: Expression, rule: Rule, parentStepIds: List<Int>): Proof {
        val step = ProofStep.RegularStep(getNextStepId(), wff, rule, parentStepIds)
        return withNewStep(step)
    }

    fun addImplicationIntroductionStep(assumptionIds: List<Int>, conclusionId: Int): Proof {
        // The expression for this step will be constructed by the validator/UI logic
        // For now, we can use a placeholder or derive it if possible.
        // Let's assume the calling code will provide the correct expression.
        // A more robust implementation might derive it here.
        val assumptionExpressions = assumptionIds.mapNotNull { id ->
            steps.find { it.id == id }?.expression 
        }
        val conclusionExpression = steps.find { it.id == conclusionId }?.expression
        
        val expression = if (assumptionExpressions.isNotEmpty() && conclusionExpression != null) {
            val combinedAssumptions = if (assumptionExpressions.size == 1) {
                assumptionExpressions.first()
            } else {
                assumptionExpressions.reduce { acc, expr -> Expression.And(acc, expr) }
            }
            Expression.Implies(combinedAssumptions, conclusionExpression)
        } else {
            // Placeholder, this should be handled more gracefully
            Expression.Variable("Error: Could not construct II expression")
        }

        val step = ProofStep.ImplicationIntroductionStep(getNextStepId(), expression, assumptionIds, conclusionId)
        return withNewStep(step)
    }

    private fun getNextStepId(): Int {
        return problem.premises.size + steps.size + 1
    }

    private fun withNewStep(step: ProofStep): Proof {
        return this.copy(steps = this.steps + step)
    }

    @Serializable
    sealed class ProofStep {
        abstract val id: Int
        abstract val expression: Expression

        @Serializable
        @SerialName("RegularStep")
        data class RegularStep(
            override val id: Int,
            override val expression: Expression,
            val rule: Rule,
            val parentStepIds: List<Int>
        ) : ProofStep()

        @Serializable
        @SerialName("Assumption")
        data class Assumption(
            override val id: Int,
            override val expression: Expression
        ) : ProofStep()

        @Serializable
        @SerialName("ImplicationIntroductionStep")
        data class ImplicationIntroductionStep(
            override val id: Int,
            override val expression: Expression,
            val assumptionIds: List<Int>,
            val conclusionOfSubProofId: Int
        ) : ProofStep()
    }
}

@Serializable
data class ProblemSet(
    val name: String,
    val problems: List<ProblemDefinition>
)
