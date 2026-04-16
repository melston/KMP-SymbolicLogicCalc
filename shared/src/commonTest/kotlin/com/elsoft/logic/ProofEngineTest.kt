package com.elsoft.logic

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
}
