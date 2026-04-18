package com.elsoft.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeneratedProblemSetParserTest {
    
    private val parser = ProblemSetParser()
    private val expParser = ExpressionParser()
    
    @Test
    fun testParse_singleProblem() {
        val text = """
            Copi 1.1
            Premises:
                k -> l
                k
            Prove:
                l
        """.trimIndent()
        
        val problemSet = parser.parse("Test Set 1", text)
        
        assertEquals("Test Set 1", problemSet.name)
        assertEquals(1, problemSet.problems.size)
        
        val problem = problemSet.problems.first()
        val premise0 = expParser.parse("k -> l")
        val premise1 = expParser.parse("k")
        val conclusion = expParser.parse("l")

        assertEquals("Copi 1.1", problem.id)
        assertEquals(2, problem.premises.size)
        assertEquals(premise0, problem.premises[0])
        assertEquals(premise1, problem.premises[1])
        assertEquals(conclusion, problem.conclusion)
    }
    
    @Test
    fun testParse_multipleProblems() {
        val text = """
            Problem A
            Premises:
                (k -> l) & (m -> n)
                k | m
            Prove:
                l | n

            Problem B
            Premises:
                ~p
            Prove:
                ~p | q
        """.trimIndent()
        
        val problemSet = parser.parse("Test Set 2", text)
        assertEquals(2, problemSet.problems.size)
        
        val problemA = problemSet.problems[0]
        val conclusionA = expParser.parse("l | n")
        assertEquals("Problem A", problemA.id)
        assertEquals(conclusionA, problemA.conclusion)
        
        val problemB = problemSet.problems[1]
        val conclusionB = expParser.parse("~p | q")
        assertEquals("Problem B", problemB.id)
        assertEquals(conclusionB, problemB.conclusion)
    }
    
    @Test
    fun testParse_noBlankLinesBetweenProblems() {
        val text = """
            Problem A
            Premises:
                p
            Prove:
                p | q
            Problem B
            Premises:
                q
            Prove:
                q | p
        """.trimIndent()
        
        val problemSet = parser.parse("Test Set 3", text)
        assertEquals(2, problemSet.problems.size)
        assertEquals("Problem A", problemSet.problems[0].id)
        assertEquals("Problem B", problemSet.problems[1].id)
    }
    
    @Test
    fun testParse_dashesBetweenProblems() {
        val text = """
            Problem A
            Premises:
                p
            Prove:
                p | q
            ---
            Problem B
            Premises:
                q
            Prove:
                q | p
           
            -------
            
            Problem C
            Premises:
                r
            Prove:
                r | s
        """.trimIndent()
        
        val problemSet = parser.parse("Test Set 4", text)
        assertEquals(3, problemSet.problems.size)
        assertEquals("Problem A", problemSet.problems[0].id)
        assertEquals("Problem B", problemSet.problems[1].id)
        assertEquals("Problem C", problemSet.problems[2].id)
    }
}