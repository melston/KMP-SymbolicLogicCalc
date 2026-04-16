package com.elsoft.logic

import kotlin.random.Random

class ProofEngine(private val random: Random = Random.Default) {
    private val premiseGenerator = PremiseGenerator(random)

    // Generator function using DetailedRule for precise backward pruning
    fun generateProblem(targetSteps: Int, requiredRules: List<Rule> = emptyList()): ProblemDefinition {
        val rules = AllRulesOfInference + AllRulesOfReplacement
        
        while (true) {
            // Start with an empty set of starting premises, we will build them uniquely
            val poolSet = mutableSetOf<Expression>()
            
            // Inject seeds for required rules if requested to speed up generation
            // Using a single call to generateInitialPool guarantees distinct expressions for p1, p2, etc.
            if (requiredRules.contains(ConstructiveDilemma)) {
                val base = premiseGenerator.generateInitialPool(4, 0)
                val p1 = base[0]; val p2 = base[1]; val p3 = base[2]; val p4 = base[3]
                poolSet.add(Expression.Or(p1, p2))
                poolSet.add(Expression.Implies(p1, p3))
                poolSet.add(Expression.Implies(p2, p4))
            }
            if (requiredRules.contains(DestructiveDilemma)) {
                val base = premiseGenerator.generateInitialPool(4, 0)
                val p1 = base[0]; val p2 = base[1]; val p3 = base[2]; val p4 = base[3]
                poolSet.add(Expression.Or(Expression.Not(p3), Expression.Not(p4)))
                poolSet.add(Expression.Implies(p1, p3))
                poolSet.add(Expression.Implies(p2, p4))
            }
            if (requiredRules.contains(ModusPonens) || requiredRules.contains(ModusTollens) || requiredRules.contains(HypotheticalSyllogism)) {
                val base = premiseGenerator.generateInitialPool(3, 1)
                val p1 = base[0]; val p2 = base[1]; val p3 = base[2]
                poolSet.add(Expression.Implies(p1, p2))
                if (requiredRules.contains(ModusTollens)) poolSet.add(Expression.Not(p2))
                if (requiredRules.contains(ModusPonens)) poolSet.add(p1)
                if (requiredRules.contains(HypotheticalSyllogism)) {
                    poolSet.add(Expression.Implies(p2, p3))
                }
            }
            if (requiredRules.contains(DisjunctiveSyllogism)) {
                val base = premiseGenerator.generateInitialPool(2, 1)
                val p1 = base[0]; val p2 = base[1]
                poolSet.add(Expression.Or(p1, p2))
                poolSet.add(Expression.Not(p1))
            }
            
            // Fill any remaining slots up to our base starting pool size (e.g. at least 3)
            while (poolSet.size < 3) {
                poolSet.addAll(premiseGenerator.generateInitialPool(1, 1))
            }
            
            val pool = poolSet.toMutableList()
            val derivations = mutableListOf<Derivation>()
            
            for (step in 0 until targetSteps) {
                val possibleDerivations = mutableListOf<Derivation>()
                
                for (rule in rules) {
                    val newDerivations = rule.apply(pool)
                    for (d in newDerivations) {
                        // Only add if it's new and doesn't make the expression too massive (prevents SOE from infinite growth)
                        if (d.result !in pool && d.result.getDepth() < 5) {
                            possibleDerivations.add(d)
                        }
                    }
                }
                
                if (possibleDerivations.isEmpty()) break
                
                // If we have required rules that haven't been met yet, heavily bias towards picking them!
                val unmetRules = requiredRules.filter { req -> derivations.none { it.rule == req } }
                
                val preferredDerivations = possibleDerivations.filter { it.rule in unmetRules }
                val chosen = if (preferredDerivations.isNotEmpty() && random.nextDouble() < 0.8) {
                    preferredDerivations.random(random) // 80% chance to pick a missing required rule if available
                } else {
                    possibleDerivations.random(random)
                }

                derivations.add(chosen)
                pool.add(chosen.result)
            }
            
            val conclusion = derivations.lastOrNull()?.result ?: pool.last()
            
            // Backward Pruning: Find all essential parents
            val usedPremises = mutableSetOf<Expression>()
            val queue = mutableListOf(conclusion)
            // We use a temporary simple step list to do the reverse traversal
            val backwardSteps = mutableListOf<Pair<Expression, Derivation>>()
            val processed = mutableSetOf<Expression>()
            
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                
                if (current in processed) continue
                processed.add(current)
                
                // Did we derive this?
                val derivation = derivations.find { it.result == current }
                if (derivation != null) {
                    if (!backwardSteps.any { it.first == derivation.result }) {
                        queue.addAll(derivation.parents)
                        backwardSteps.add(Pair(derivation.result, derivation))
                    }
                } else {
                    // It must be a starting premise
                    usedPremises.add(current)
                }
            }
            
            // Check if we hit the user's required rules
            val usedRules = backwardSteps.map { it.second.rule }.toSet()
            val containsRequiredRules = requiredRules.all { it in usedRules }
            
            // Ensure the problem isn't completely trivial (e.g. given P prove P, or only 1 step)
            // also ensures we generated at least `targetSteps/2` essential steps so the problem is meaty.
            val premisesList = usedPremises.toList()
            if (containsRequiredRules && 
                backwardSteps.size >= maxOf(2, targetSteps / 2) && 
                !usedPremises.contains(conclusion) &&
                !hasObviousConflicts(premisesList)
            ) {
                // To keep the generator interface simple, we just return the core ProblemDefinition.
                // The backward steps (the solution) could theoretically be packaged as a Proof object, 
                // but since this engine's goal is just to generate solvable problems, 
                // returning the ProblemDefinition is sufficient for the user to then try to solve.
                return ProblemDefinition(
                    id = "Generated Problem", // Could use UUID
                    premises = premisesList,
                    conclusion = conclusion
                )
            }
            
            // If the generated problem was trivial, conflicted, or missing rules, we loop and try again!
        }
    }

    /**
     * Checks if the premise pool contains obvious logical contradictions 
     * like both `P` and `~P` existing simultaneously.
     * While it does not check for deep contradictions (which is computationally expensive),
     * it prevents blatantly artificial starting states.
     */
    private fun hasObviousConflicts(premises: List<Expression>): Boolean {
        val premiseSet = premises.toSet()
        for (p in premiseSet) {
            if (p is Expression.Not && premiseSet.contains(p.operand)) {
                return true
            }
            if (premiseSet.contains(Expression.Not(p))) {
                return true
            }
        }
        return false
    }
}
