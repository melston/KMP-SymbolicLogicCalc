package com.elsoft.symlogic.logic

/**
 * Represents the definition of a symbolic logic problem, containing only the
 * essential information: what is given and what needs to be proven.
 * This is the canonical representation for any problem, whether it was
 * authored in a file or generated dynamically.
 *
 * @param id A unique identifier for the problem (e.g., "Copi 3.4 #2" or "Generated Problem #123").
 * @param premises A list of given expressions that are assumed to be true.
 * @param conclusion The target expression that needs to be proven from the premises.
 */
data class ProblemDefinition(
    val id: String,
    val premises: List<Expression>,
    val conclusion: Expression
)

/**
 * Represents a user's step-by-step solution to a given problem.
 * The proof is initialized with the problem's premises, and the user
 * adds new steps to derive the conclusion.
 *
 * @param problem The problem definition this proof is attempting to solve.
 * @param steps The list of logical steps taken by the user.
 */
data class Proof(
    val problem: ProblemDefinition,
    val steps: List<ProofStep>
) {
    /**
     * A sealed class representing a single step within a proof.
     * Each step derives a new expression by applying a rule to one or more previous steps.
     */
    sealed class ProofStep {
        abstract val id: String
        abstract val expression: Expression

        /**
         * A regular step applying a rule to parent expressions.
         * @param id A unique identifier for this line in the proof (e.g., "L1", "L2").
         * @param expression The newly derived expression.
         * @param rule The rule that was applied to derive the expression.
         * @param parentStepIds The list of step IDs that this step depends on.
         *                      These can reference either initial premises (e.g., "P1") or previous steps.
         */
        data class RegularStep(
            override val id: String,
            override val expression: Expression,
            val rule: Rule,
            val parentStepIds: List<String>
        ) : ProofStep()

        /**
         * An assumption step, marking the beginning of a sub-proof.
         * @param id A unique identifier for this assumption (e.g., "A1").
         * @param expression The assumed premise for the sub-proof.
         */
        data class Assumption(
            override val id: String,
            override val expression: Expression
        ) : ProofStep()

        /**
         * An Implication Introduction step, closing a sub-proof.
         * @param id A unique identifier for this step.
         * @param expression The resulting implication ((Assumption1 & Assumption2 & ...) -> ConclusionOfSubProof).
         * @param assumptionIds The list of IDs of the Assumption steps that started this sub-proof.
         * @param conclusionOfSubProofId The ID of the last step within the sub-proof that forms the consequent of the implication.
         */
        data class ImplicationIntroductionStep(
            override val id: String,
            override val expression: Expression,
            val assumptionIds: List<String>, // Changed from singular assumptionId
            val conclusionOfSubProofId: String
        ) : ProofStep()
    }
}

/**
 * A collection of authored problems, typically read from a single file or source.
 *
 * @param name The name of the entire problem set (e.g., "Copi Chapter 3, Section 4").
 * @param problems The list of individual problem definitions in the set.
 */
data class ProblemSet(
    val name: String,
    val problems: List<ProblemDefinition>
)
