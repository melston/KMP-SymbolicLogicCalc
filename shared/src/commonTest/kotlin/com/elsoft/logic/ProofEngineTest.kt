package com.elsoft.logic

import com.elsoft.logic.ConstructiveDilemma
import com.elsoft.logic.ModusPonens
import com.elsoft.logic.ProofEngine
import kotlin.test.Test
import kotlin.test.assertTrue

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
            
            println("\nSolution:")
            problem.solutionSteps.forEach { step ->
                println("  ${step.derivedExpression}  [${step.ruleUsed.name} from ${step.parentExpressions.joinToString(", ")}]")
            }
            println("==================\n")

            // Every problem should have a conclusion
            assertTrue(problem.conclusion != null)
            
            // If it generated steps, it must have premises
            if (problem.solutionSteps.isNotEmpty()) {
                assertTrue(problem.premises.isNotEmpty(), "Problem has steps but no premises!")
            }
            
            // Validate that the problem isn't trivial (Given P, Prove P) 
            // unless the engine failed to take any steps
            if (problem.solutionSteps.isNotEmpty()) {
                assertTrue(!problem.premises.contains(problem.conclusion), "Conclusion trivially exists in premises!")
            }
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
        
        println("\nSolution:")
        problem.solutionSteps.forEach { step ->
            println("  ${step.derivedExpression}  [${step.ruleUsed.name} from ${step.parentExpressions.joinToString(", ")}]")
        }
        println("==================\n")
        
        val usedRules = problem.solutionSteps.map { it.ruleUsed }.toSet()
        
        assertTrue(required.all { it in usedRules }, "The generated problem did not contain all required rules!")
    }
}
