package logic

object ModusPonens : Rule {
    override val name = "Modus Ponens"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Implies) {
                // If we have P -> Q, look for P
                if (expressions.contains(e1.left)) {
                    results.add(Derivation(e1.right, this, listOf(e1, e1.left)))
                }
            }
        }
        return results
    }
}

object ModusTollens : Rule {
    override val name = "Modus Tollens"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Implies) {
                // If we have P -> Q, look for ~Q
                val notQ = Expression.Not(e1.right)
                if (expressions.contains(notQ)) {
                    results.add(Derivation(Expression.Not(e1.left), this, listOf(e1, notQ)))
                }
            }
        }
        return results
    }
}

object HypotheticalSyllogism : Rule {
    override val name = "Hypothetical Syllogism"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Implies) {
                for (e2 in expressions) {
                    if (e1 != e2 && e2 is Expression.Implies && e1.right == e2.left) {
                        // P -> Q, Q -> R yields P -> R
                        results.add(Derivation(Expression.Implies(e1.left, e2.right), this, listOf(e1, e2)))
                    }
                }
            }
        }
        return results
    }
}

object DisjunctiveSyllogism : Rule {
    override val name = "Disjunctive Syllogism"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Or) {
                // P | Q, ~P yields Q
                val notP = Expression.Not(e1.left)
                if (expressions.contains(notP)) {
                    results.add(Derivation(e1.right, this, listOf(e1, notP)))
                }
                
                // P | Q, ~Q yields P
                val notQ = Expression.Not(e1.right)
                if (expressions.contains(notQ)) {
                    results.add(Derivation(e1.left, this, listOf(e1, notQ)))
                }
            }
        }
        return results
    }
}

object Simplification : Rule {
    override val name = "Simplification"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e in expressions) {
            if (e is Expression.And) {
                results.add(Derivation(e.left, this, listOf(e)))
                results.add(Derivation(e.right, this, listOf(e)))
            }
        }
        return results
    }
}

object Conjunction : Rule {
    override val name = "Conjunction"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (i in expressions.indices) {
            for (j in i + 1 until expressions.size) {
                val e1 = expressions[i]
                val e2 = expressions[j]
                
                results.add(Derivation(Expression.And(e1, e2), this, listOf(e1, e2)))
                results.add(Derivation(Expression.And(e2, e1), this, listOf(e1, e2)))
            }
        }
        return results
    }
}

object Addition : Rule {
    override val name = "Addition"
    
    // Addition is notoriously difficult in forward generation. It creates an infinite space of "P | X".
    // If X is an unguessable complex string, the problem looks artificial and unsolvable for a human.
    // To prevent this, we heavily restrict Addition. We ONLY allow addition of single variables (e.g. `p`, `~q`) 
    // that are already present in the basic context of the problem, rather than combining entire complex statements together randomly.
    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        
        // Extract a pool of simple variables/not variables from current expressions
        val simpleTerms = mutableSetOf<Expression>()
        for (e in expressions) {
            extractSimpleTerms(e, simpleTerms)
        }
        
        for (e1 in expressions) {
            for (term in simpleTerms) {
                if (e1 != term) {
                    // Only list e1 as the parent since the rule of addition logically only depends on e1.
                    results.add(Derivation(Expression.Or(e1, term), this, listOf(e1))) 
                    results.add(Derivation(Expression.Or(term, e1), this, listOf(e1)))
                }
            }
        }
        return results
    }
    
    private fun extractSimpleTerms(e: Expression, terms: MutableSet<Expression>) {
        when (e) {
            is Expression.Variable -> terms.add(e)
            is Expression.Not -> {
                if (e.operand is Expression.Variable) {
                    terms.add(e)
                } else {
                    extractSimpleTerms(e.operand, terms)
                }
            }
            is Expression.And -> { extractSimpleTerms(e.left, terms); extractSimpleTerms(e.right, terms) }
            is Expression.Or -> { extractSimpleTerms(e.left, terms); extractSimpleTerms(e.right, terms) }
            is Expression.Implies -> { extractSimpleTerms(e.left, terms); extractSimpleTerms(e.right, terms) }
            is Expression.Iff -> { extractSimpleTerms(e.left, terms); extractSimpleTerms(e.right, terms) }
        }
    }
}

object ConstructiveDilemma : Rule {
    override val name = "Constructive Dilemma"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Or) { // P | Q
                for (e2 in expressions) {
                    if (e2 is Expression.Implies && e2.left == e1.left) { // P -> R
                        for (e3 in expressions) {
                            if (e3 is Expression.Implies && e3.left == e1.right) { // Q -> S
                                results.add(Derivation(Expression.Or(e2.right, e3.right), this, listOf(e1, e2, e3)))
                            }
                        }
                    }
                }
            }
        }
        return results
    }
}

object DestructiveDilemma : Rule {
    override val name = "Destructive Dilemma"

    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (e1 in expressions) {
            if (e1 is Expression.Or) { // ~R | ~S
                val leftNot = e1.left as? Expression.Not
                val rightNot = e1.right as? Expression.Not
                
                if (leftNot != null && rightNot != null) {
                    for (e2 in expressions) {
                        if (e2 is Expression.Implies && e2.right == leftNot.operand) { // P -> R
                            for (e3 in expressions) {
                                if (e3 is Expression.Implies && e3.right == rightNot.operand) { // Q -> S
                                    results.add(Derivation(
                                        Expression.Or(Expression.Not(e2.left), Expression.Not(e3.left)),
                                        this, 
                                        listOf(e1, e2, e3)
                                    ))
                                }
                            }
                        }
                    }
                }
            }
        }
        return results
    }
}

val AllRulesOfInference = listOf(
    ModusPonens, ModusTollens, HypotheticalSyllogism, DisjunctiveSyllogism,
    Simplification, Conjunction, Addition, ConstructiveDilemma, DestructiveDilemma
)
