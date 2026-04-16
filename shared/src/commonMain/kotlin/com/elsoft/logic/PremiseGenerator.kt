package com.elsoft.logic

import kotlin.random.Random

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
            // Base case: return a variable or a negated variable
            val variable = variables.random(random)
            return if (random.nextBoolean()) Expression.Not(variable) else variable
        }

        // Randomly choose the type of expression to generate
        return when (random.nextInt(6)) {
            0 -> Expression.Not(generateRandomExpression(depth + 1, maxDepth))
            1 -> Expression.And(
                generateRandomExpression(depth + 1, maxDepth),
                generateRandomExpression(depth + 1, maxDepth)
            )
            2 -> Expression.Or(
                generateRandomExpression(depth + 1, maxDepth),
                generateRandomExpression(depth + 1, maxDepth)
            )
            3 -> Expression.Implies(
                generateRandomExpression(depth + 1, maxDepth),
                generateRandomExpression(depth + 1, maxDepth)
            )
            4 -> Expression.Iff(
                generateRandomExpression(depth + 1, maxDepth),
                generateRandomExpression(depth + 1, maxDepth)
            )
            else -> variables.random(random) // Occasional simple variable
        }
    }
}
