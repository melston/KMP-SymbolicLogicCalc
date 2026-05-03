package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.logic.*
import com.elsoft.symlogic.problems.Proof
import com.elsoft.symlogic.problems.parsers.ExpressionParser

@Composable
fun ProofInput(
    proof: Proof,
    initialSelectedIds: Set<Int>,
    isSubProofActive: Boolean,
    onAddStep: (expression: String, rule: Rule, parentIds: String) -> Unit,
    onAddAssumption: (expression: String) -> Unit,
    onCloseSubProof: () -> Unit
) {
    var expressionText by remember { mutableStateOf("") }
    var parentIdsText by remember { mutableStateOf(initialSelectedIds.joinToString(", ")) }
    
    val allRules = remember { AllRulesOfInference + AllRulesOfReplacement }
    val expressionParser = remember { ExpressionParser() }

    // This is the key change: Derive the parent expressions reactively based on the text input.
    val parentExpressions = remember(parentIdsText, proof) {
        val allAvailableSteps = (proof.problem.premises.mapIndexed { index, expr -> (index + 1) to expr } +
                                 proof.steps.map { it.id to it.expression }).toMap()
        
        parentIdsText.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull { id -> allAvailableSteps[id]?.let { id to it } }
            .toMap()
    }

    val validRules = remember(parentExpressions) {
        if (parentExpressions.isEmpty()) {
            allRules
        } else {
            allRules.filter { rule ->
                if (rule == Addition) {
                    parentExpressions.size == 1
                } else {
                    rule.apply(parentExpressions.values.toList()).isNotEmpty()
                }
            }
        }
    }

    var selectedRule by remember(validRules) { mutableStateOf(validRules.firstOrNull()) }
    var ruleMenuExpanded by remember { mutableStateOf(false) }

    val possibleOutcomes = remember(selectedRule, parentExpressions) {
        if (selectedRule != Addition) {
            selectedRule?.apply(parentExpressions.values.toList())?.map { it.result }?.toSet()
        } else {
            null // We don't pre-calculate for Addition
        }
    }

    val parsedExpression = remember(expressionText) {
        try { expressionParser.parse(expressionText) } catch (e: Exception) { null }
    }

    val isAddStepEnabled = expressionText.isNotEmpty() &&
                           selectedRule != null &&
                           parsedExpression != null &&
                           (
                               if (selectedRule == Addition) {
                                   selectedRule!!.validate(parsedExpression, parentExpressions.values.toList())
                               } else {
                                   possibleOutcomes?.contains(parsedExpression) ?: false
                               }
                           )

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        OutlinedTextField(
            value = expressionText,
            onValueChange = { expressionText = it },
            label = { Text("New WFF") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedRule?.name ?: "No valid rules",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rule") },
                    trailingIcon = {
                        Button(onClick = { ruleMenuExpanded = true }, enabled = validRules.isNotEmpty()) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(expanded = ruleMenuExpanded, onDismissRequest = { ruleMenuExpanded = false }) {
                    validRules.forEach { rule ->
                        DropdownMenuItem(onClick = {
                            selectedRule = rule
                            ruleMenuExpanded = false
                        }) {
                            Text(rule.name)
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = parentIdsText,
                onValueChange = { parentIdsText = it },
                label = { Text("Parent IDs") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    selectedRule?.let {
                        onAddStep(expressionText, it, parentIdsText)
                        expressionText = ""
                        parentIdsText = ""
                    }
                },
                enabled = isAddStepEnabled
            ) {
                Text("Add Step")
            }
            Button(onClick = {
                onAddAssumption(expressionText)
                expressionText = ""
                parentIdsText = ""
            }) {
                Text("Add Assumption")
            }
            Button(
                onClick = {
                    onCloseSubProof()
                    expressionText = ""
                    parentIdsText = ""
                },
                enabled = isSubProofActive
            ) {
                Text("Close Sub-proof (II)")
            }
        }
    }
}
