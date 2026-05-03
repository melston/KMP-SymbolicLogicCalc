package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elsoft.symlogic.logic.AllRulesOfInference
import com.elsoft.symlogic.logic.AllRulesOfReplacement
import com.elsoft.symlogic.logic.Expression
import com.elsoft.symlogic.logic.Rule
import com.elsoft.symlogic.logic.Addition
import com.elsoft.symlogic.problems.parsers.ExpressionParser

@Composable
fun ProofInput(
    selectedParentExpressions: Map<Int, Expression>,
    isSubProofActive: Boolean,
    onAddStep: (expression: String, rule: Rule, parentIds: String) -> Unit,
    onAddAssumption: (expression: String) -> Unit,
    onCloseSubProof: () -> Unit
) {
    var expressionText by remember { mutableStateOf("") }
    var parentIdsText by remember { mutableStateOf(selectedParentExpressions.keys.joinToString()) }
    
    val allRules = remember { AllRulesOfInference + AllRulesOfReplacement }
    val expressionParser = remember { ExpressionParser() }

    val validRules = remember(selectedParentExpressions) {
        if (selectedParentExpressions.isEmpty()) {
            allRules
        } else {
            allRules.filter { rule ->
                // For Addition, it's always potentially valid if one parent is selected.
                if (rule == Addition) {
                    selectedParentExpressions.size == 1
                } else {
                    rule.apply(selectedParentExpressions.values.toList()).isNotEmpty()
                }
            }
        }
    }

    var selectedRule by remember(validRules) { mutableStateOf(validRules.firstOrNull()) }
    var ruleMenuExpanded by remember { mutableStateOf(false) }

    val possibleOutcomes = remember(selectedRule, selectedParentExpressions) {
        if (selectedRule != Addition) {
            selectedRule?.apply(selectedParentExpressions.values.toList())?.map { it.result }?.toSet()
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
                                   // For Addition, validate directly since outcomes are infinite
                                   selectedRule!!.validate(parsedExpression, selectedParentExpressions.values.toList())
                               } else {
                                   // For other rules, check against the pre-calculated set
                                   possibleOutcomes?.contains(parsedExpression) ?: false
                               }
                           )

    LaunchedEffect(selectedParentExpressions) {
        parentIdsText = selectedParentExpressions.keys.sorted().joinToString(", ")
    }

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
