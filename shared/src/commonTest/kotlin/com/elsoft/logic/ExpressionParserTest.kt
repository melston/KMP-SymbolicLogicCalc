package com.elsoft.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpressionParserTest {

    private val parser = ExpressionParser()

    @Test
    fun testParse_simpleVariable() {
        val result = parser.parse("p")
        assertEquals(Expression.Variable("p"), result)
    }

    @Test
    fun testParse_simpleNegation() {
        val result = parser.parse("~p")
        assertEquals(Expression.Not(Expression.Variable("p")), result)
    }

    @Test
    fun testParse_simpleAnd() {
        val result = parser.parse("p & q")
        assertEquals(Expression.And(Expression.Variable("p"), Expression.Variable("q")), result)
    }

    @Test
    fun testParse_simpleOr() {
        val result = parser.parse("p | q")
        assertEquals(Expression.Or(Expression.Variable("p"), Expression.Variable("q")), result)
    }

    @Test
    fun testParse_simpleImplies() {
        val result = parser.parse("p -> q")
        assertEquals(Expression.Implies(Expression.Variable("p"), Expression.Variable("q")), result)
    }

    @Test
    fun testParse_simpleIff() {
        val result = parser.parse("p <-> q")
        assertEquals(Expression.Iff(Expression.Variable("p"), Expression.Variable("q")), result)
    }

    @Test
    fun testParse_complexExpressionWithPrecedence() {
        // p & q | r -> s
        val result = parser.parse("p & q | r -> s")
        val expected = Expression.Implies(
            Expression.Or(
                Expression.And(
                    Expression.Variable("p"),
                    Expression.Variable("q")
                ),
                Expression.Variable("r")
            ),
            Expression.Variable("s")
        )
        assertEquals(expected, result)
    }

    @Test
    fun testParse_expressionWithParentheses() {
        // p & (q | r)
        val result = parser.parse("p & (q | r)")
        val expected = Expression.And(
            Expression.Variable("p"),
            Expression.Or(
                Expression.Variable("q"),
                Expression.Variable("r")
            )
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParse_deeplyNestedExpression() {
        val result = parser.parse("~(p -> (q & ~r)) <-> s")
        val expected = Expression.Iff(
            Expression.Not(
                Expression.Implies(
                    Expression.Variable("p"),
                    Expression.And(
                        Expression.Variable("q"),
                        Expression.Not(Expression.Variable("r"))
                    )
                )
            ),
            Expression.Variable("s")
        )
        assertEquals(expected, result)
    }

    @Test
    fun testParse_invalidInput_throwsException() {
        assertFailsWith<ExpressionParser.ParseException> {
            parser.parse("p & & q")
        }
        assertFailsWith<ExpressionParser.ParseException> {
            parser.parse("(p & q")
        }
        assertFailsWith<ExpressionParser.ParseException> {
            parser.parse("p -> > q")
        }
    }
}

class GeneratedProblemSetParserTest {
    
    private val parser = ProblemSetParser()
    
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
        assertEquals("Copi 1.1", problem.id)
        assertEquals(2, problem.premises.size)
        assertEquals(Expression.Implies(Expression.Variable("k"), Expression.Variable("l")), problem.premises[0])
        assertEquals(Expression.Variable("k"), problem.premises[1])
        assertEquals(Expression.Variable("l"), problem.conclusion)
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
        assertEquals("Problem A", problemA.id)
        assertEquals(Expression.Or(Expression.Variable("l"), Expression.Variable("n")), problemA.conclusion)
        
        val problemB = problemSet.problems[1]
        assertEquals("Problem B", problemB.id)
        assertEquals(Expression.Or(Expression.Not(Expression.Variable("p")), Expression.Variable("q")), problemB.conclusion)
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
