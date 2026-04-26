package com.elsoft.symlogic.problems.parsers

import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.logic.normalizeWffString

/**
 * A basic recursive descent parser to convert a string representation of a
 * Well-Formed Formula (WFF) into an Expression tree.
 *
 * Supported Operators (in order of precedence, highest to lowest):
 * 1. Parentheses: ()
 * 2. Not: ~
 * 3. And: &
 * 4. Or: |
 * 5. Implies: ->
 * 6. Iff: <->
 */
class ExpressionParser {

    class ParseException(message: String) : Exception(message)

    /**
     * Parses a string like `(p & ~q) -> r` or `(p ∧ q) → r` into an Expression.
     * It first normalizes the string to a canonical ASCII format.
     */
    fun parse(input: String): Expression {
        val normalizedInput = normalizeWffString(input)
        val tokens = tokenize(normalizedInput)
        if (tokens.isEmpty()) throw ParseException("Empty input string")
        val (expr, remainingTokens) = parseIff(tokens)
        if (remainingTokens.isNotEmpty()) {
            throw ParseException("Unexpected trailing tokens: ${remainingTokens.joinToString("")}")
        }
        return expr
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < input.length) {
            when (val c = input[i]) {
                ' ', '\t', '\n', '\r' -> i++ // Skip whitespace
                '(', ')', '~', '&', '|' -> {
                    tokens.add(c.toString())
                    i++
                }
                '-' -> {
                    if (i + 1 < input.length && input[i + 1] == '>') {
                        tokens.add("->")
                        i += 2
                    } else {
                        throw ParseException("Unexpected character '-' at index $i, expected '->'")
                    }
                }
                '<' -> {
                    if (i + 2 < input.length && input.substring(i, i + 3) == "<->") {
                        tokens.add("<->")
                        i += 3
                    } else {
                        throw ParseException("Unexpected character '<' at index $i, expected '<->'")
                    }
                }
                else -> {
                    if (c.isLetter()) { // Variable names can only be letters for now
                        val start = i
                        while (i < input.length && input[i].isLetter()) {
                            i++
                        }
                        tokens.add(input.substring(start, i))
                    } else {
                        throw ParseException("Invalid character: $c at index $i")
                    }
                }
            }
        }
        return tokens
    }

    // Recursive Descent Parsing Rules

    // Iff (<->)
    private fun parseIff(tokens: List<String>): Pair<Expression, List<String>> {
        var (expr, remaining) = parseImplies(tokens)
        while (remaining.isNotEmpty() && remaining[0] == "<->") {
            val (rightExpr, rightRemaining) = parseImplies(remaining.drop(1))
            expr = Expression.Iff(expr, rightExpr)
            remaining = rightRemaining
        }
        return Pair(expr, remaining)
    }

    // Implies (->)
    private fun parseImplies(tokens: List<String>): Pair<Expression, List<String>> {
        var (expr, remaining) = parseOr(tokens)
        while (remaining.isNotEmpty() && remaining[0] == "->") {
            val (rightExpr, rightRemaining) = parseOr(remaining.drop(1))
            expr = Expression.Implies(expr, rightExpr)
            remaining = rightRemaining
        }
        return Pair(expr, remaining)
    }

    // Or (|)
    private fun parseOr(tokens: List<String>): Pair<Expression, List<String>> {
        var (expr, remaining) = parseAnd(tokens)
        while (remaining.isNotEmpty() && remaining[0] == "|") {
            val (rightExpr, rightRemaining) = parseAnd(remaining.drop(1))
            expr = Expression.Or(expr, rightExpr)
            remaining = rightRemaining
        }
        return Pair(expr, remaining)
    }

    // And (&)
    private fun parseAnd(tokens: List<String>): Pair<Expression, List<String>> {
        var (expr, remaining) = parseUnary(tokens)
        while (remaining.isNotEmpty() && remaining[0] == "&") {
            val (rightExpr, rightRemaining) = parseUnary(remaining.drop(1))
            expr = Expression.And(expr, rightExpr)
            remaining = rightRemaining
        }
        return Pair(expr, remaining)
    }

    // Unary Operators (~, parens, variables)
    private fun parseUnary(tokens: List<String>): Pair<Expression, List<String>> {
        if (tokens.isEmpty()) throw ParseException("Unexpected end of input expecting unary expression")

        val token = tokens[0]
        return when {
            token == "~" -> {
                val (expr, remaining) = parseUnary(tokens.drop(1))
                Pair(Expression.Not(expr), remaining)
            }
            token == "(" -> {
                val (expr, remaining) = parseIff(tokens.drop(1)) // Start back at highest precedence
                if (remaining.isEmpty() || remaining[0] != ")") {
                    throw ParseException("Missing closing parenthesis ')'")
                }
                Pair(expr, remaining.drop(1))
            }
            token.all { it.isLetter() } -> { // It's a variable
                Pair(Expression.Variable(token), tokens.drop(1))
            }
            else -> throw ParseException("Unexpected token: $token")
        }
    }
}
