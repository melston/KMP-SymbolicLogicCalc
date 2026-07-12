package com.elsoft.symlogic.logic

import com.elsoft.symlogic.problems.ProofEngine
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class ProofEngineTest {

    @Test
    fun testGenerateProblem_isAlwaysSolvableAndNonTrivial() {
        val engine = ProofEngine()

        // Generate 10 random problems
        for (i in 0 until 10) {
            val problem = engine.generateProblem(targetSteps = 4)
            
            println("=== Problem $i ===")
            println("Premises:")
            problem.premises.forEach { println("  $it") }
            println("Prove: ${problem.conclusion}")
            
            println("==================\n")

            assertNotNull(problem.conclusion)
            assertTrue(problem.premises.isNotEmpty(), "Problem must have premises!")
            assertTrue(!problem.premises.contains(problem.conclusion), "Conclusion trivially exists in premises!")
        }
    }
    
    @Test
    fun testGenerateProblem_withRequiredRules() {
        val engine = ProofEngine()
        val required = listOf(ConstructiveDilemma, ModusPonens)
        
        println("Attempting to generate a problem that MUST use Constructive Dilemma and Modus Ponens...")
        
        val problem = engine.generateProblem(targetSteps = 5, requiredRules = required)
        
        println("=== Required Rules Problem ===")
        println("Premises:")
        problem.premises.forEach { println("  $it") }
        println("Prove: ${problem.conclusion}")
        println("==================\n")
        
        assertNotNull(problem.conclusion)
        assertTrue(problem.premises.isNotEmpty())
    }

    @Test
    fun testGenerateProblem_withManyRequiredRules() {
        val engine = ProofEngine()
        val required = listOf(ConstructiveDilemma, ModusPonens, ModusTollens)
        
        println("Attempting to generate a problem that MUST use Constructive Dilemma, Modus Ponens, and Modus Tollens...")
        
        val problem = engine.generateProblem(targetSteps = 6, requiredRules = required)
        
        println("=== Many Required Rules Problem ===")
        println("Premises:")
        problem.premises.forEach { println("  $it") }
        println("Prove: ${problem.conclusion}")
        println("==================\n")
        
        assertNotNull(problem.conclusion)
        assertTrue(problem.premises.isNotEmpty())
        
        // Assert that no premise contains double negation like "~~s"
        fun hasDoubleNegation(e: Expression): Boolean {
            return when (e) {
                is Expression.Not -> e.operand is Expression.Not || hasDoubleNegation(e.operand)
                is Expression.And -> hasDoubleNegation(e.left) || hasDoubleNegation(e.right)
                is Expression.Or -> hasDoubleNegation(e.left) || hasDoubleNegation(e.right)
                is Expression.Implies -> hasDoubleNegation(e.left) || hasDoubleNegation(e.right)
                is Expression.Iff -> hasDoubleNegation(e.left) || hasDoubleNegation(e.right)
                is Expression.Variable -> false
            }
        }
        
        problem.premises.forEach { p ->
            assertTrue(!hasDoubleNegation(p), "Premise '$p' contains a double negation, which is likely a trivial tautology!")
        }
    }
}
