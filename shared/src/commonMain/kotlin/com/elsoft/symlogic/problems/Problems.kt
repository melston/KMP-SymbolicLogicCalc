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
    val steps: List<ProofStep>
) {
    @Serializable
    sealed class ProofStep {
        abstract val id: String
        abstract val expression: Expression

        @Serializable
        @SerialName("RegularStep")
        data class RegularStep(
            override val id: String,
            override val expression: Expression,
            val rule: Rule, // Rule must also be serializable
            val parentStepIds: List<String>
        ) : ProofStep()

        @Serializable
        @SerialName("Assumption")
        data class Assumption(
            override val id: String,
            override val expression: Expression
        ) : ProofStep()

        @Serializable
        @SerialName("ImplicationIntroductionStep")
        data class ImplicationIntroductionStep(
            override val id: String,
            override val expression: Expression,
            val assumptionIds: List<String>,
            val conclusionOfSubProofId: String
        ) : ProofStep()
    }
}

@Serializable
data class ProblemSet(
    val name: String,
    val problems: List<ProblemDefinition>
)
