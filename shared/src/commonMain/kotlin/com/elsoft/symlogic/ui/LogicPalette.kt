package com.elsoft.symlogic.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsoft.symlogic.problems.LogicOperator
import com.elsoft.symlogic.problems.Vars


// 1. First, define the helper button composable
@Composable
fun SymbolButton(
    symbol: String,
    backgroundColor: Color,
    onClick: (String) -> Unit
) {
    Button(
        onClick = { onClick(symbol) },
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth() // Makes buttons uniform in the grid
    ) {
        Text(text = symbol, fontSize = 18.sp)
    }
}

@Composable
fun LogicPalette(
    onSymbolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val operatorColor = MaterialTheme.colors.primary
    val variableColor = MaterialTheme.colors.secondary
    val sectionTitleStyle = MaterialTheme.typography.subtitle1

    val operators = listOf(
        LogicOperator.Negation, LogicOperator.Conjunction,
        LogicOperator.Disjunction, LogicOperator.Implication,
        LogicOperator.Iff, LogicOperator.StartGroup, LogicOperator.EndGroup
    )

    val variables = Vars.variables

    // Use a LazyVerticalGrid for a clean layout
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.padding(8.dp)
    ) {
        // Section 1 Header
        item(span = { GridItemSpan(4) }) {
            Text(
                "Operators",
                style = sectionTitleStyle,
                modifier = Modifier.padding(vertical = 8.dp)
            ) // Changed from titleSmall to subtitle1
        }

        items(operators) { op ->
            SymbolButton(
                symbol = op.display,
                backgroundColor = operatorColor,
                onClick = onSymbolSelected
            )
        }

        // Section 2 Header
        item(span = { GridItemSpan(4) }) {
            Text(
                "Variables",
                style = sectionTitleStyle,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(variables) { variable ->
            SymbolButton(
                symbol = variable.name,
                backgroundColor = variableColor,
                onClick = onSymbolSelected
            )
        }
    }
}
