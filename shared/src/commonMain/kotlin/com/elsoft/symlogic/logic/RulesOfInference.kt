package com.elsoft.symlogic.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@SerialName("ModusPonens")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 2) return false
        val (e1, e2) = parentExpressions

        // Check for P -> Q and P
        if (e1 is Expression.Implies && e1.left == e2 && e1.right == derivedExpression) {
            return true
        }
        // Check for P and P -> Q (order might be different)
        if (e2 is Expression.Implies && e2.left == e1 && e2.right == derivedExpression) {
            return true
        }
        return false
    }
}

@Serializable
@SerialName("ModusTollens")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 2) return false
        val (e1, e2) = parentExpressions

        // Check for P -> Q and ~Q
        if (e1 is Expression.Implies && e2 is Expression.Not && e1.right == e2.operand && derivedExpression == Expression.Not(e1.left)) {
            return true
        }
        // Check for ~Q and P -> Q (order might be different)
        if (e2 is Expression.Implies && e1 is Expression.Not && e2.right == e1.operand && derivedExpression == Expression.Not(e2.left)) {
            return true
        }
        return false
    }
}

@Serializable
@SerialName("HypotheticalSyllogism")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 2) return false
        val (e1, e2) = parentExpressions

        // Check for P -> Q and Q -> R
        if (e1 is Expression.Implies && e2 is Expression.Implies && e1.right == e2.left && derivedExpression == Expression.Implies(e1.left, e2.right)) {
            return true
        }
        // Check for Q -> R and P -> Q (order might be different)
        if (e2 is Expression.Implies && e1 is Expression.Implies && e2.right == e1.left && derivedExpression == Expression.Implies(e2.left, e1.right)) {
            return true
        }
        return false
    }
}

@Serializable
@SerialName("DisjunctiveSyllogism")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 2) return false
        val (e1, e2) = parentExpressions

        // Case 1: P | Q and ~P yields Q
        if (e1 is Expression.Or && e2 is Expression.Not && e1.left == e2.operand && derivedExpression == e1.right) {
            return true
        }
        // Case 2: P | Q and ~Q yields P
        if (e1 is Expression.Or && e2 is Expression.Not && e1.right == e2.operand && derivedExpression == e1.left) {
            return true
        }
        // Case 3: ~P and P | Q yields Q (order might be different)
        if (e2 is Expression.Or && e1 is Expression.Not && e2.left == e1.operand && derivedExpression == e2.right) {
            return true
        }
        // Case 4: ~Q and P | Q yields P (order might be different)
        if (e2 is Expression.Or && e1 is Expression.Not && e2.right == e1.operand && derivedExpression == e2.left) {
            return true
        }
        return false
    }
}

@Serializable
@SerialName("Simplification")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 1) return false
        val parent = parentExpressions.first()

        return parent is Expression.And && (parent.left == derivedExpression || parent.right == derivedExpression)
    }
}

@Serializable
@SerialName("Conjunction")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 2) return false
        val (e1, e2) = parentExpressions

        // Check if derived is (e1 & e2) or (e2 & e1)
        return derivedExpression == Expression.And(e1, e2) || derivedExpression == Expression.And(e2, e1)
    }
}

@Serializable
@SerialName("Addition")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 1) return false
        val parent = parentExpressions.first()

        // Derived must be an OR expression
        if (derivedExpression !is Expression.Or) return false

        // One side of the OR must be the parent expression
        return derivedExpression.left == parent || derivedExpression.right == parent
    }
}

@Serializable
@SerialName("ConstructiveDilemma")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 3) return false
        val (e_or, e_implies1, e_implies2) = parentExpressions

        // We need P|Q, P->R, Q->S to derive R|S
        if (e_or is Expression.Or && e_implies1 is Expression.Implies && e_implies2 is Expression.Implies) {
            // Check if e_implies1 is P->R and e_implies2 is Q->S
            if (e_or.left == e_implies1.left && e_or.right == e_implies2.left && derivedExpression == Expression.Or(e_implies1.right, e_implies2.right)) {
                return true
            }
            // Check if e_implies1 is Q->S and e_implies2 is P->R (order might be different)
            if (e_or.left == e_implies2.left && e_or.right == e_implies1.left && derivedExpression == Expression.Or(e_implies2.right, e_implies1.right)) {
                return true
            }
        }
        return false
    }
}

@Serializable
@SerialName("DestructiveDilemma")
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

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 3) return false
        val (e_or, e_implies1, e_implies2) = parentExpressions

        // We need ~R | ~S, P->R, Q->S to derive ~P | ~Q
        if (e_or is Expression.Or && e_implies1 is Expression.Implies && e_implies2 is Expression.Implies) {
            val notR = e_or.left as? Expression.Not
            val notS = e_or.right as? Expression.Not

            if (notR != null && notS != null) {
                // Check if e_implies1 is P->R and e_implies2 is Q->S
                if (e_implies1.right == notR.operand && e_implies2.right == notS.operand && derivedExpression == Expression.Or(Expression.Not(e_implies1.left), Expression.Not(e_implies2.left))) {
                    return true
                }
                // Check if e_implies1 is Q->S and e_implies2 is P->R (order might be different)
                if (e_implies1.right == notS.operand && e_implies2.right == notR.operand && derivedExpression == Expression.Or(Expression.Not(e_implies2.left), Expression.Not(e_implies1.left))) {
                    return true
                }
            }
        }
        return false
    }
}

val AllRulesOfInference = listOf(
    ModusPonens, ModusTollens, HypotheticalSyllogism, DisjunctiveSyllogism,
    Simplification, Conjunction, Addition, ConstructiveDilemma, DestructiveDilemma
)
