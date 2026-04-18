package com.elsoft.symlogic.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofValidatorTest {
    val p = ExpressionParser()

    @Test
    fun testValidProof_ModusPonens() {
        val problem = ProblemDefinition(
            id = "MP-1",
            premises = listOf(
                p.parse("P -> Q"),
                p.parse("P")
            ),
            conclusion = p.parse("Q")
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.RegularStep(
                    id = "L1",
                    expression = p.parse("Q"),
                    rule = ModusPonens,
                    parentStepIds = listOf("P1", "P2")
                )
            )
        )

        val validator = ProofValidator()
        val result = validator.validate(proof)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun testValidProof_SubProof_ImplicationIntroduction_SingleAssumption() {
        // Prove: P -> (P | Q)
        val problem = ProblemDefinition(
            id = "II-1",
            premises = listOf(),
            conclusion = Expression.Implies(
                p.parse("P"),
                p.parse("P | Q")
            )
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(
                    id = "A1",
                    expression = p.parse("P")
                ),
                Proof.ProofStep.RegularStep(
                    id = "L1",
                    expression = p.parse("P | Q"),
                    rule = Addition,
                    parentStepIds = listOf("A1") 
                ),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = "L2",
                    expression = Expression.Implies(
                        p.parse("P"),
                        p.parse("P | Q")
                    ),
                    assumptionIds = listOf("A1"),
                    conclusionOfSubProofId = "L1"
                )
            )
        )

        val validator = ProofValidator()
        val result = validator.validate(proof)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun testValidProof_SubProof_ImplicationIntroduction_MultipleAssumptions() {
        // Prove: (P & Q) -> P
        val problem = ProblemDefinition(
            id = "II-2",
            premises = listOf(),
            conclusion = Expression.Implies(
                p.parse("P & Q"),
                p.parse("P")
            )
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(
                    id = "A1",
                    expression = p.parse("P")
                ),
                Proof.ProofStep.Assumption(
                    id = "A2",
                    expression = p.parse("Q")
                ),
                Proof.ProofStep.RegularStep(
                    id = "L1",
                    expression = p.parse("P & Q"),
                    rule = Conjunction,
                    parentStepIds = listOf("A1", "A2") 
                ),
                Proof.ProofStep.RegularStep(
                    id = "L2",
                    expression = p.parse("P"),
                    rule = Simplification,
                    parentStepIds = listOf("L1")
                ),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = "L3",
                    expression = p.parse("(P & Q) -> P"),
                    assumptionIds = listOf("A1", "A2"),
                    conclusionOfSubProofId = "L2"
                )
            )
        )

        val validator = ProofValidator()
        val result = validator.validate(proof)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun testInvalidProof_ReferencingClosedSubProof() {
        val problem = ProblemDefinition(
            id = "Invalid-1",
            premises = listOf(),
            conclusion = p.parse("P")
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(id = "A1", expression = Expression.Variable("P")),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = "L1",
                    expression = p.parse("(P -> P)"),
                    assumptionIds = listOf("A1"),
                    conclusionOfSubProofId = "A1"
                ),
                // Try to use A1 after the sub-proof was closed!
                Proof.ProofStep.RegularStep(
                    id = "L2",
                    expression = p.parse("P"),
                    rule = Addition, // Just some rule trying to illegally use A1
                    parentStepIds = listOf("A1") 
                )
            )
        )

        val validator = ProofValidator()
        val result = validator.validate(proof)
        assertTrue(result is ValidationResult.Invalid)
        // Should complain about A1 not being accessible
        assertTrue(result.reason.contains("not accessible") || result.reason.contains("does not exist"))
    }
}
