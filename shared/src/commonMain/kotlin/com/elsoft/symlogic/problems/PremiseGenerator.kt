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
        val pool = mutableSetOf<Expression>()
        
        // Ensure we at least have a couple of simple variables so rules can fire
        // Since we are adding to a set, if it randomly picks the same variable twice,
        // the while loop below will naturally fill in the missing slots until we reach `poolSize`.
        if (poolSize >= 2) {
            pool.add(variables.random(random))
            pool.add(variables.random(random))
        }

        while (pool.size < poolSize) {
            pool.add(generateRandomExpression(depth = 0, maxDepth = maxDepth))
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
            0 -> Expression.Not(generateRandomExpression(depth + 1, maxDepth))
            1 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (left == right || left == Expression.Not(right))
                Expression.And(left, right)
            }
            2 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (left == right || left == Expression.Not(right))
                Expression.Or(left, right)
            }
            3 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (left == right || left == Expression.Not(right))
                Expression.Implies(left, right)
            }
            4 -> {
                var left: Expression
                var right: Expression
                do {
                    left = generateRandomExpression(depth + 1, maxDepth)
                    right = generateRandomExpression(depth + 1, maxDepth)
                } while (left == right || left == Expression.Not(right))
                Expression.Iff(left, right)
            }
            else -> {
                val variable = variables.random(random)
                if (random.nextBoolean()) Expression.Not(variable) else variable
            }
        }
    }
}
