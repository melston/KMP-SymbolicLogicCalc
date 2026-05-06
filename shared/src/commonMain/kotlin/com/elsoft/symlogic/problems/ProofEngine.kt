package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.AllRulesOfInference
import com.elsoft.symlogic.logic.AllRulesOfReplacement
import com.elsoft.symlogic.logic.ConstructiveDilemma
import com.elsoft.symlogic.logic.Derivation
import com.elsoft.symlogic.logic.DestructiveDilemma
import com.elsoft.symlogic.logic.DisjunctiveSyllogism
import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.logic.HypotheticalSyllogism
import com.elsoft.symlogic.logic.ModusPonens
import com.elsoft.symlogic.logic.ModusTollens
import com.elsoft.symlogic.logic.Rule
import kotlin.random.Random

/**
 * Engine responsible for generating solvable symbolic logic problems
 * of varying complexity using rules of inference and replacement.
 */
class ProofEngine(private val random: Random = Random.Default) {
    private val premiseGenerator = PremiseGenerator(random)
    private val maxAttempts = 50000  // Maximum number of attempts to generate a problem

    /**
     * Generates a solvable problem with a target number of steps.
     *
     * @param targetSteps The desired number of logical steps to reach the conclusion.
     * @param requiredRules A list of rules that must be used in the solution.
     * @return A [ProblemDefinition] containing premises and a conclusion.
     */
    fun generateProblem(targetSteps: Int, requiredRules: List<Rule> = emptyList()): ProblemDefinition {
        val rules = AllRulesOfInference + AllRulesOfReplacement

        for (attempt in 0 until maxAttempts) {
            // Start with an empty set of starting premises, we will build them uniquely
            val premiseList = generatePremises(requiredRules).toMutableList()

            // A list of derivations we can make from the current premise pool.
            val derivations = generatePossibleDerivations(
                targetSteps,
                rules,
                premiseList,
                requiredRules
            )

            val conclusion = derivations.lastOrNull()?.result ?: premiseList.last()

            // Backward Pruning: Find all essential parents
            val usedPremises = mutableSetOf<Expression>()
            // Expressions we need to find the parents of
            val queue = mutableListOf(conclusion)
            // Expressions we have seen already.  Prevents looking at the same expression twice.
            val processed = mutableSetOf<Expression>()
            // We use a temporary simple step list to do the reverse traversal
            val essentialDerivations = mutableListOf<Pair<Expression, Derivation>>()

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()

                if (current in processed) continue
                processed.add(current)

                // Did we derive this?
                val derivation = derivations.find { it.result == current }
                if (derivation != null) {
                    if (!essentialDerivations.any { it.first == derivation.result }) {
                        queue.addAll(derivation.parents)
                        essentialDerivations.add(Pair(derivation.result, derivation))
                    }
                } else {
                    // It must be a starting premise
                    usedPremises.add(current)
                }
            }

            // Check if we hit the user's required rules
            val usedRules = essentialDerivations.map { it.second.rule }.toSet()
            val containsRequiredRules = requiredRules.all { it in usedRules }

            // Ensure the problem isn't completely trivial (e.g. given P prove P, or only 1 step)
            // also ensures we generated at least `targetSteps/2` essential steps so the problem is meaty.
            val premisesList = usedPremises.toList()
            if (containsRequiredRules &&
                essentialDerivations.size >= maxOf(2, targetSteps / 2) &&
                !usedPremises.contains(conclusion) &&
                !hasObviousConflicts(premisesList)
            ) {
                // To keep the generator interface simple, we just return the core ProblemDefinition.
                // The backward steps (the solution) could theoretically be packaged as a Proof object,
                // but since this engine's goal is just to generate solvable problems,
                // returning the ProblemDefinition is sufficient for the user to then try to solve.
                val uuid = (1..8).map { random.nextInt(0, 16).toString(16) }.joinToString("")
                return ProblemDefinition(
                    id = "gen_$uuid",
                    premises = premisesList,
                    conclusion = conclusion
                )
            }

            // If the generated problem was trivial, conflicted, or missing rules,
            // we loop and try again!
        }

        throw Exception("Bailed after $maxAttempts iterations.")
    }

    /**
     * Generates a list of possible derivations from the current premise pool.
     *
     * @param targetSteps The number of steps to attempt to derive.
     * @param rules The set of rules available for derivation.
     * @param premiseList The current pool of expressions (premises + previous derivations).
     * @param requiredRules Rules that should ideally be included in the derivation path.
     * @return A list of [Derivation] objects representing the logical steps taken.
     */
    private fun generatePossibleDerivations(
        targetSteps: Int,
        rules: List<Rule>,
        premiseList: MutableList<Expression>,
        requiredRules: List<Rule>
    ) : MutableList<Derivation> {
        val derivations = mutableListOf<Derivation>()
        for (step in 0 until targetSteps) {
            val possibleDerivations = mutableListOf<Derivation>()

            for (rule in rules) {
                val newDerivations = rule.apply(premiseList)
                for (d in newDerivations) {
                    // Only add if it's new and doesn't make the expression too massive (prevents SOE from infinite growth)
                    if (d.result !in premiseList && d.result.getDepth() < 5) {
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
            premiseList.add(chosen.result)
        }
        return derivations
    }

    /**
     * Seeds the premise pool based on required rules to increase the likelihood
     * of generating a valid problem that utilizes those specific rules.
     */
    private fun generatePremises(requiredRules: List<Rule>) : MutableSet<Expression> {
        val poolSet = mutableSetOf<Expression>()
        if (requiredRules.contains(ConstructiveDilemma)) {
            val base = premiseGenerator.generateInitialPool(4, 0)
            val p1 = base[0];
            val p2 = base[1];
            val p3 = base[2];
            val p4 = base[3]
            poolSet.add(Expression.Or(p1, p2))
            poolSet.add(Expression.Implies(p1, p3))
            poolSet.add(Expression.Implies(p2, p4))
        }

        if (requiredRules.contains(DestructiveDilemma)) {
            val base = premiseGenerator.generateInitialPool(4, 0)
            val p1 = base[0];
            val p2 = base[1];
            val p3 = base[2];
            val p4 = base[3]
            poolSet.add(Expression.Or(Expression.Not(p3), Expression.Not(p4)))
            poolSet.add(Expression.Implies(p1, p3))
            poolSet.add(Expression.Implies(p2, p4))
        }

        if (requiredRules.contains(ModusPonens) || requiredRules.contains(ModusTollens) || requiredRules.contains(
                HypotheticalSyllogism
            )
        ) {
            val base = premiseGenerator.generateInitialPool(3, 1)
            val p1 = base[0];
            val p2 = base[1];
            val p3 = base[2]
            poolSet.add(Expression.Implies(p1, p2))
            if (requiredRules.contains(ModusTollens)) poolSet.add(Expression.Not(p2))
            if (requiredRules.contains(ModusPonens)) poolSet.add(p1)
            if (requiredRules.contains(HypotheticalSyllogism)) {
                poolSet.add(Expression.Implies(p2, p3))
            }
        }

        if (requiredRules.contains(DisjunctiveSyllogism)) {
            val base = premiseGenerator.generateInitialPool(2, 1)
            val p1 = base[0];
            val p2 = base[1]
            poolSet.add(Expression.Or(p1, p2))
            poolSet.add(Expression.Not(p1))
        }

        // Fill any remaining slots up to our base starting pool size (e.g. at least 3)
        while (poolSet.size < 3) {
            poolSet.addAll(premiseGenerator.generateInitialPool(1, 1))
        }

        return poolSet
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