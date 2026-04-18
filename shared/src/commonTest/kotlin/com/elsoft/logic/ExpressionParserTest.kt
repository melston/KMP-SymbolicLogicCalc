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
