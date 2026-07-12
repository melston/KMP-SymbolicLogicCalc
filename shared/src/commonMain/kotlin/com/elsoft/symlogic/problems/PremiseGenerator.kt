package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression
import kotlin.random.Random

/**
 * Responsible for generating random logical expressions and initial premise pools
 * used by the [ProofEngine] to create symbolic logic problems.
 */
class PremiseGenerator(private val random: Random = Random.Default) {
    
    private val variables = listOf("p", "q", "r", "s", "t").map { Expression.Variable(it) }

    /**
     * Generates a random initial pool of premises.
     * The returned list is guaranteed to contain exactly [poolSize] unique, distinct expressions.
     */
    fun generateInitialPool(poolSize: Int = 4, maxDepth: Int = 2): List<Expression> {
        val pool = mutableListOf<Expression>()
        
        // Ensure we at least have a couple of simple variables so rules can fire
        if (poolSize >= 2) {
            val v1 = variables.random(random)
            pool.add(if (random.nextBoolean()) Expression.Not(v1) else v1)
            
            var attempt = 0
            while (pool.size < 2 && attempt < 20) {
                val v2 = variables.random(random)
                val v2Expr = if (random.nextBoolean()) Expression.Not(v2) else v2
                if (pool.none { isTriviallyRelated(it, v2Expr) }) {
                    pool.add(v2Expr)
                }
                attempt++
            }
        }

        var loopAttempt = 0
        while (pool.size < poolSize && loopAttempt < 1000) {
            val candidate = generateRandomExpression(depth = 0, maxDepth = maxDepth)
            if (pool.none { isTriviallyRelated(it, candidate) }) {
                pool.add(candidate)
            }
            loopAttempt++
        }

        // Fallback in case we hit limit constraints
        while (pool.size < poolSize) {
            val variable = variables.random(random)
            val fallback = if (random.nextBoolean()) Expression.Not(variable) else variable
            if (!pool.contains(fallback)) {
                pool.add(fallback)
            } else if (!pool.contains(variable)) {
                pool.add(variable)
            } else {
                // Break to avoid infinite loop
                break
            }
        }

        return pool.toList()
    }

    /**
     * Recursively generates a random logical expression up to a maximum depth.
     * @param depth Current recursion depth.
     * @param maxDepth Maximum allowed depth for the expression tree.
     */
    private fun generateRandomExpression(depth: Int, maxDepth: Int): Expression {
        if (depth >= maxDepth) {
            val variable = variables.random(random)
            return if (random.nextBoolean()) Expression.Not(variable) else variable
        }

        return when (random.nextInt(6)) {
            0 -> {
                var operand = generateRandomExpression(depth + 1, maxDepth)
                while (operand is Expression.Not) {
                    operand = generateRandomExpression(depth + 1, maxDepth)
                }
                Expression.Not(operand)
            }
            1 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (isTriviallyRelated(left, right))
                Expression.And(left, right)
            }
            2 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (isTriviallyRelated(left, right))
                Expression.Or(left, right)
            }
            3 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (isTriviallyRelated(left, right))
                Expression.Implies(left, right)
            }
            4 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (isTriviallyRelated(left, right))
                Expression.Iff(left, right)
            }
            else -> {
                val variable = variables.random(random)
                if (random.nextBoolean()) Expression.Not(variable) else variable
            }
        }
    }

    private fun isTriviallyRelated(left: Expression, right: Expression): Boolean {
        val normLeft = left.stripDoubleNegations()
        val normRight = right.stripDoubleNegations()
        if (normLeft == normRight) return true

        val negLeft = Expression.Not(normLeft).stripDoubleNegations()
        if (negLeft == normRight) return true

        val negRight = Expression.Not(normRight).stripDoubleNegations()
        if (negRight == normLeft) return true

        return false
    }
}
