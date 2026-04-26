package com.elsoft.symlogic.problems

import com.elsoft.symlogic.logic.Expression

object Vars {
    val variables = listOf("p", "q", "r", "s", "t").map { Expression.Variable(it) }
}


sealed class LogicOperator(val display: String, val alternate: String) {
    object Implication : LogicOperator("→", "->")
    object Conjunction : LogicOperator("∧", "&")
    object Disjunction : LogicOperator("∨", "|")
    object Negation : LogicOperator("¬", "~")
    object Iff : LogicOperator("↔", "<->")
    object StartGroup : LogicOperator("(", "(")
    object EndGroup : LogicOperator(")", ")")
}
