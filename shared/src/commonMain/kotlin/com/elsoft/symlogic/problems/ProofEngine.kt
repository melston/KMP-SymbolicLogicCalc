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
        val rules = AllRulesOfInference + AllRulesOfReplacement.filter { it in requiredRules }

        for (attempt in 0 until maxAttempts) {
            val premiseList = generatePremises(requiredRules).toMutableList()

            // Oversample derivations to ensure we have a rich graph to search for a valid sub-proof.
            val derivationSteps = maxOf(targetSteps + 3, targetSteps * 2)
            val derivations = generatePossibleDerivations(
                derivationSteps,
                rules,
                premiseList,
                requiredRules
            )

            // Search through the derived expressions (from newest to oldest) to find one that
            // forms a valid proof using all required rules and has a good step count.
            for (derivation in derivations.reversed()) {
                val conclusion = derivation.result
                val queue = mutableListOf(conclusion)
                val processed = mutableSetOf<Expression>()
                val essentialDerivations = mutableListOf<Pair<Expression, Derivation>>()
                val usedPremises = mutableSetOf<Expression>()

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    if (current in processed) continue
                    processed.add(current)

                    val d = derivations.find { it.result == current }
                    if (d != null) {
                        if (!essentialDerivations.any { it.first == d.result }) {
                            queue.addAll(d.parents)
                            essentialDerivations.add(Pair(d.result, d))
                        }
                    } else {
                        usedPremises.add(current)
                    }
                }

                val usedRules = essentialDerivations.map { it.second.rule }.toSet()
                val containsRequiredRules = requiredRules.all { it in usedRules }
                val premisesList = usedPremises.toList()

                if (containsRequiredRules &&
                    essentialDerivations.size in maxOf(2, targetSteps / 2)..(targetSteps + 3) &&
                    !usedPremises.contains(conclusion) &&
                    !hasObviousConflicts(premisesList)
                ) {
                    val uuid = (1..8).map { random.nextInt(0, 16).toString(16) }.joinToString("")
                    return ProblemDefinition(
                        id = "gen_$uuid",
                        premises = premisesList,
                        conclusion = conclusion
                    )
                }
            }
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

            val unmetRules = requiredRules.filter { req -> derivations.none { it.rule == req } }
            val preferredDerivations = possibleDerivations.filter { it.rule in unmetRules }

            val chosen = if (preferredDerivations.isNotEmpty() && random.nextDouble() < 0.95) {
                // 95% chance to pick an unmet required rule from all possible derivations
                preferredDerivations.random(random)
            } else {
                // Otherwise, prioritize keeping the proof connected
                val derivedSet = derivations.map { it.result }.toSet()
                val connectedDerivations = if (step == 0) possibleDerivations else possibleDerivations.filter { d ->
                    d.parents.any { it in derivedSet }
                }
                val candidates = if (connectedDerivations.isNotEmpty()) connectedDerivations else possibleDerivations
                candidates.random(random)
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
        
        // Generate a shared pool of base variables.
        val basePool = premiseGenerator.generateInitialPool(5, 0)
        var baseIdx = 0
        fun getNextBase(count: Int): List<Expression> {
            val list = (0 until count).map { basePool[(baseIdx + it) % basePool.size] }
            baseIdx += count
            return list
        }

        if (requiredRules.contains(ConstructiveDilemma)) {
            val base = getNextBase(4)
            val p1 = base[0]
            val p2 = base[1]
            val p3 = base[2]
            val p4 = base[3]
            poolSet.add(Expression.Or(p1, p2))
            poolSet.add(Expression.Implies(p1, p3))
            poolSet.add(Expression.Implies(p2, p4))
        }

        if (requiredRules.contains(DestructiveDilemma)) {
            val base = getNextBase(4)
            val p1 = base[0]
            val p2 = base[1]
            val p3 = base[2]
            val p4 = base[3]
            poolSet.add(Expression.Or(p3.negate(), p4.negate()))
            poolSet.add(Expression.Implies(p1, p3))
            poolSet.add(Expression.Implies(p2, p4))
        }

        if (requiredRules.contains(ModusPonens) || requiredRules.contains(ModusTollens) || requiredRules.contains(
                HypotheticalSyllogism
            )
        ) {
            val base = getNextBase(3)
            val p1 = base[0]
            val p2 = base[1]
            val p3 = base[2]
            poolSet.add(Expression.Implies(p1, p2))
            if (requiredRules.contains(ModusTollens)) poolSet.add(p2.negate())
            if (requiredRules.contains(ModusPonens)) poolSet.add(p1)
            if (requiredRules.contains(HypotheticalSyllogism)) {
                poolSet.add(Expression.Implies(p2, p3))
            }
        }

        if (requiredRules.contains(DisjunctiveSyllogism)) {
            val base = getNextBase(2)
            val p1 = base[0]
            val p2 = base[1]
            poolSet.add(Expression.Or(p1, p2))
            poolSet.add(p1.negate())
        }

        // Fill any remaining slots up to our base starting pool size (e.g. at least 4)
        while (poolSet.size < 4) {
            poolSet.add(premiseGenerator.generateInitialPool(1, 1).first())
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
            if (p is Expression.Implies &&
                (p.right == Expression.Not(p.left) ||
                 p.left == Expression.Not(p.right))) {
                // p -> ~p or ~p -> p
                // This is a valid expression, evaluating to ~p.  However, it is
                // odd looking.  So, don't allow it most of the time.
                return if (random.nextDouble() < 0.3) true
                       else false
            }
        }
        return false
    }
}