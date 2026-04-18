package com.elsoft.logic

/**
 * Rules of Replacement apply to sub-expressions as well as whole expressions.
 * They are bi-directional logically, but for a generator, we treat them as
 * one-way transformations to keep things simple.
 */
interface ReplacementRule : Rule {
    /**
     * Attempts to apply this replacement rule anywhere within the given expression.
     */
    fun applyToExpression(expression: Expression): List<Derivation>
}

// Helper to easily traverse and replace sub-expressions.
// It recursively applies a transformation function `transform` to every node in
// the expression tree.
fun Expression.replaceAll(
    rule: ReplacementRule,
    parent: Expression,
    depth: Int = 0,
    transform: (Expression) -> Expression?
): List<Derivation> {
    if (depth > 15) return emptyList() // Prevent stack overflow on deeply nested expressions

    val results = mutableListOf<Derivation>()

    // Try applying at the root
    val rootTransform = transform(this)
    if (rootTransform != null && rootTransform != this) {
        results.add(Derivation(rootTransform, rule, listOf(parent)))
    }

    // Try applying to children recursively
    when (this) {
        is Expression.Not -> {
            results.addAll(this.operand.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Not(it.result), rule, listOf(parent))
            })
        }
        is Expression.And -> {
            results.addAll(this.left.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.And(it.result, this.right), rule, listOf(parent))
            })
            results.addAll(this.right.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.And(this.left, it.result), rule, listOf(parent))
            })
        }
        is Expression.Or -> {
            results.addAll(this.left.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Or(it.result, this.right), rule, listOf(parent))
            })
            results.addAll(this.right.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Or(this.left, it.result), rule, listOf(parent))
            })
        }
        is Expression.Implies -> {
            results.addAll(this.left.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Implies(it.result, this.right), rule, listOf(parent))
            })
            results.addAll(this.right.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Implies(this.left, it.result), rule, listOf(parent))
            })
        }
        is Expression.Iff -> {
            results.addAll(this.left.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Iff(it.result, this.right), rule, listOf(parent))
            })
            results.addAll(this.right.replaceAll(rule, parent, depth + 1, transform).map {
                Derivation(Expression.Iff(this.left, it.result), rule, listOf(parent))
            })
        }
        is Expression.Variable -> {} // Variables have no sub-expressions
    }

    return results
}

abstract class BaseReplacementRule : ReplacementRule {
    override fun apply(expressions: List<Expression>): List<Derivation> {
        val results = mutableListOf<Derivation>()
        for (expr in expressions) {
            results.addAll(applyToExpression(expr))
        }
        return results
    }

    override fun validate(derivedExpression: Expression, parentExpressions: List<Expression>): Boolean {
        if (parentExpressions.size != 1) return false
        val parent = parentExpressions.first()
        // Replacement rules can be applied to any sub-expression. 
        // We validate by checking if the derived expression is one of the possible 
        // applications of the rule to the parent.
        return applyToExpression(parent).any { it.result == derivedExpression }
    }
}

object DeMorgan : BaseReplacementRule() {
    override val name = "De Morgan's Laws"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // ~(P & Q) == ~P | ~Q
                is Expression.Not -> {
                    when (val inner = e.operand) {
                        is Expression.And -> Expression.Or(Expression.Not(inner.left), Expression.Not(inner.right))
                        is Expression.Or -> Expression.And(Expression.Not(inner.left), Expression.Not(inner.right))
                        else -> null
                    }
                }
                // ~P | ~Q == ~(P & Q)
                is Expression.Or -> {
                    if (e.left is Expression.Not && e.right is Expression.Not) {
                        Expression.Not(Expression.And(e.left.operand, e.right.operand))
                    } else null
                }
                // ~P & ~Q == ~(P | Q)
                is Expression.And -> {
                    if (e.left is Expression.Not && e.right is Expression.Not) {
                        Expression.Not(Expression.Or(e.left.operand, e.right.operand))
                    } else null
                }
                else -> null
            }
        }
    }
}

object Commutativity : BaseReplacementRule() {
    override val name = "Commutativity"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                is Expression.And -> Expression.And(e.right, e.left)
                is Expression.Or -> Expression.Or(e.right, e.left)
                is Expression.Iff -> Expression.Iff(e.right, e.left)
                else -> null
            }
        }
    }
}

object Associativity : BaseReplacementRule() {
    override val name = "Associativity"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                is Expression.And -> {
                    // P & (Q & R) -> (P & Q) & R
                    if (e.right is Expression.And) {
                        Expression.And(Expression.And(e.left, e.right.left), e.right.right)
                    }
                    // (P & Q) & R -> P & (Q & R)
                    else if (e.left is Expression.And) {
                        Expression.And(e.left.left, Expression.And(e.left.right, e.right))
                    } else null
                }
                is Expression.Or -> {
                    // P | (Q | R) -> (P | Q) | R
                    if (e.right is Expression.Or) {
                        Expression.Or(Expression.Or(e.left, e.right.left), e.right.right)
                    }
                    // (P | Q) | R -> P | (Q | R)
                    else if (e.left is Expression.Or) {
                        Expression.Or(e.left.left, Expression.Or(e.left.right, e.right))
                    } else null
                }
                else -> null
            }
        }
    }
}

object Distribution : BaseReplacementRule() {
    override val name = "Distribution"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                is Expression.And -> {
                    // P & (Q | R) -> (P & Q) | (P & R)
                    if (e.right is Expression.Or) {
                        Expression.Or(Expression.And(e.left, e.right.left), Expression.And(e.left, e.right.right))
                    }
                    // (P | Q) & R -> (P & R) | (Q & R)
                    else if (e.left is Expression.Or) {
                        Expression.Or(Expression.And(e.left.left, e.right), Expression.And(e.left.right, e.right))
                    } else null
                }
                is Expression.Or -> {
                    // P | (Q & R) -> (P | Q) & (P | R)
                    if (e.right is Expression.And) {
                        Expression.And(Expression.Or(e.left, e.right.left), Expression.Or(e.left, e.right.right))
                    }
                    // (P & Q) | R -> (P | R) & (Q | R)
                    else if (e.left is Expression.And) {
                        Expression.And(Expression.Or(e.left.left, e.right), Expression.Or(e.left.right, e.right))
                    } else null
                }
                else -> null
            }
        }
    }
}

object DoubleNegation : BaseReplacementRule() {
    override val name = "Double Negation"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // ~~P -> P
                is Expression.Not -> {
                    if (e.operand is Expression.Not) {
                        e.operand.operand
                    } else null
                }
                // P -> ~~P
                else -> Expression.Not(Expression.Not(e))
            }
        }
    }
}

object Transposition : BaseReplacementRule() {
    override val name = "Transposition"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // P -> Q == ~Q -> ~P
                is Expression.Implies -> Expression.Implies(Expression.Not(e.right), Expression.Not(e.left))
                else -> null
            }
        }
    }
}

object MaterialImplication : BaseReplacementRule() {
    override val name = "Material Implication"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // P -> Q == ~P | Q
                is Expression.Implies -> Expression.Or(Expression.Not(e.left), e.right)
                // ~P | Q == P -> Q
                is Expression.Or -> {
                    if (e.left is Expression.Not) {
                        Expression.Implies(e.left.operand, e.right)
                    } else null
                }
                else -> null
            }
        }
    }
}

object MaterialEquivalence : BaseReplacementRule() {
    override val name = "Material Equivalence"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // P <-> Q == (P -> Q) & (Q -> P)
                is Expression.Iff -> Expression.And(Expression.Implies(e.left, e.right), Expression.Implies(e.right, e.left))
                // (P -> Q) & (Q -> P) == P <-> Q
                is Expression.And -> {
                    if (e.left is Expression.Implies && e.right is Expression.Implies) {
                        if (e.left.left == e.right.right && e.left.right == e.right.left) {
                            Expression.Iff(e.left.left, e.left.right)
                        } else null
                    } else null
                }
                else -> null
            }
        }
    }
}

object Exportation : BaseReplacementRule() {
    override val name = "Exportation"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // (P & Q) -> R == P -> (Q -> R)
                is Expression.Implies -> {
                    if (e.left is Expression.And) {
                        Expression.Implies(e.left.left, Expression.Implies(e.left.right, e.right))
                    } else if (e.right is Expression.Implies) {
                        // P -> (Q -> R) == (P & Q) -> R
                        Expression.Implies(Expression.And(e.left, e.right.left), e.right.right)
                    } else null
                }
                else -> null
            }
        }
    }
}

object Tautology : BaseReplacementRule() {
    override val name = "Tautology"

    override fun applyToExpression(expression: Expression): List<Derivation> {
        return expression.replaceAll(this, expression) { e ->
            when (e) {
                // P | P == P
                is Expression.Or -> {
                    if (e.left == e.right) e.left else null
                }
                // P & P == P
                is Expression.And -> {
                    if (e.left == e.right) e.left else null
                }
                // P == P & P or P | P
                else -> null // Note: We could return List here but replaceAll expects single return.
            }
        }
    }
}

val AllRulesOfReplacement = listOf(
    DeMorgan, Commutativity, Associativity, Distribution, DoubleNegation,
    Transposition, MaterialImplication, MaterialEquivalence, Exportation, Tautology
)
