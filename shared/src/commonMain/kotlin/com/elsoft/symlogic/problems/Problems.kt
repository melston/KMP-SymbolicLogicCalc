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
    /**
     * Returns the next available ID for a new step.
     * It's calculated as the number of premises + the number of existing steps + 1.
     */
    fun getNextStepId(): Int {
        return problem.premises.size + steps.size + 1
    }

    /**
     * Creates a new Proof instance with the added step.
     * This immutable approach is safer for state management in Compose.
     */
    fun withNewStep(step: ProofStep): Proof {
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
