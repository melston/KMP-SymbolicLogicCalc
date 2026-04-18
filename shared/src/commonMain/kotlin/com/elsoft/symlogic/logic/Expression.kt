package com.elsoft.symlogic.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class Expression {
    @Serializable
    @SerialName("Variable")
    data class Variable(val name: String) : Expression() {
        override fun toString(): String = name
    }
    
    @Serializable
    @SerialName("Not")
    data class Not(val operand: Expression) : Expression() {
        override fun toString(): String = "~$operand"
    }
    
    @Serializable
    @SerialName("And")
    data class And(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "($left & $right)"
    }
    
    @Serializable
    @SerialName("Or")
    data class Or(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "($left | $right)"
    }
    
    @Serializable
    @SerialName("Implies")
    data class Implies(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "($left -> $right)"
    }
    
    @Serializable
    @SerialName("Iff")
    data class Iff(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "($left <-> $right)"
    }
    
    // Helper to calculate depth
    fun getDepth(): Int {
        return when (this) {
            is Variable -> 1
            is Not -> 1 + operand.getDepth()
            is And -> 1 + maxOf(left.getDepth(), right.getDepth())
            is Or -> 1 + maxOf(left.getDepth(), right.getDepth())
            is Implies -> 1 + maxOf(left.getDepth(), right.getDepth())
            is Iff -> 1 + maxOf(left.getDepth(), right.getDepth())
        }
    }
}
