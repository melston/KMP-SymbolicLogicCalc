package logic

data class Problem(
    val premises: List<Expression>,
    val conclusion: Expression,
    val solutionSteps: List<Step>
)

data class Step(
    val derivedExpression: Expression,
    val ruleUsed: Rule,
    val parentExpressions: List<Expression>
)
