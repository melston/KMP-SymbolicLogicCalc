package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.logic.ModusPonens
import com.elsoft.symlogic.logic.Addition
import com.elsoft.symlogic.logic.Conjunction
import com.elsoft.symlogic.logic.Simplification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofValidatorTest {

    @Test
    fun testValidProof_ModusPonens() {
        val problem = ProblemDefinition(
            id = "MP-1",
            premises = listOf(
                Expression.Implies(Expression.Variable("P"), Expression.Variable("Q")),
                Expression.Variable("P")
            ),
            conclusion = Expression.Variable("Q")
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.RegularStep(
                    id = 3, // Premises are 1 and 2
                    expression = Expression.Variable("Q"),
                    rule = ModusPonens,
                    parentStepIds = listOf(1, 2)
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
                Expression.Variable("P"), 
                Expression.Or(Expression.Variable("P"), Expression.Variable("Q"))
            )
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(
                    id = 1,
                    expression = Expression.Variable("P")
                ),
                Proof.ProofStep.RegularStep(
                    id = 2,
                    expression = Expression.Or(Expression.Variable("P"), Expression.Variable("Q")),
                    rule = Addition,
                    parentStepIds = listOf(1) 
                ),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = 3,
                    expression = Expression.Implies(
                        Expression.Variable("P"), 
                        Expression.Or(Expression.Variable("P"), Expression.Variable("Q"))
                    ),
                    assumptionIds = listOf(1),
                    conclusionOfSubProofId = 2
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
                Expression.And(Expression.Variable("P"), Expression.Variable("Q")), 
                Expression.Variable("P")
            )
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(
                    id = 1,
                    expression = Expression.Variable("P")
                ),
                Proof.ProofStep.Assumption(
                    id = 2,
                    expression = Expression.Variable("Q")
                ),
                Proof.ProofStep.RegularStep(
                    id = 3,
                    expression = Expression.And(Expression.Variable("P"), Expression.Variable("Q")),
                    rule = Conjunction,
                    parentStepIds = listOf(1, 2) 
                ),
                Proof.ProofStep.RegularStep(
                    id = 4,
                    expression = Expression.Variable("P"),
                    rule = Simplification,
                    parentStepIds = listOf(3)
                ),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = 5,
                    expression = Expression.Implies(
                        Expression.And(Expression.Variable("P"), Expression.Variable("Q")), 
                        Expression.Variable("P")
                    ),
                    assumptionIds = listOf(1, 2),
                    conclusionOfSubProofId = 4
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
            conclusion = Expression.Variable("P")
        )

        val proof = Proof(
            problem = problem,
            steps = listOf(
                Proof.ProofStep.Assumption(id = 1, expression = Expression.Variable("P")),
                Proof.ProofStep.ImplicationIntroductionStep(
                    id = 2,
                    expression = Expression.Implies(Expression.Variable("P"), Expression.Variable("P")),
                    assumptionIds = listOf(1),
                    conclusionOfSubProofId = 1
                ),
                // Try to use step 1 after the sub-proof was closed!
                Proof.ProofStep.RegularStep(
                    id = 3,
                    expression = Expression.Variable("P"),
                    rule = Simplification, // Just some rule trying to illegally use step 1
                    parentStepIds = listOf(1) 
                )
            )
        )

        val validator = ProofValidator()
        val result = validator.validate(proof)
        assertTrue(result is ValidationResult.Invalid)
        // Should complain about step 1 not being accessible
        assertTrue(result.reason.contains("not accessible") || result.reason.contains("does not exist"))
    }
}
