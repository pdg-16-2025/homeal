package com.example.homeal_app.ui.Fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.homeal_app.model.Ingredient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

class FridgeFragment : Fragment() {

    private val fridgeViewModel: FridgeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FridgeScreen(viewModel = fridgeViewModel)
                }
            }
        }
    }
}

@Composable
fun FridgeScreen(viewModel: FridgeViewModel) {
    val ingredients by viewModel.ingredients.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { viewModel.showDialog() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Add Ingredient")
        }
        // Ingredient list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = ingredients,
                key = { it.name }
            ) { ingredient ->
                Column {
                    IngredientItem(
                        ingredient = ingredient,
                        onRemove = { viewModel.removeIngredient(ingredient) },
                        onQuantityChange = { q, u ->
                            viewModel.updateIngredientQuantity(ingredient.name, q, u)
                        }
                    )
                    if (ingredient != ingredients.last()) {
                        Divider()
                    }
                }
            }
        }


        if (showAddDialog) {
            AddIngredientDialog(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                availableIngredients = viewModel.getFilteredAvailableIngredients(),
                onIngredientSelected = viewModel::addIngredient,
                onDismiss = viewModel::hideDialog
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onRemove: () -> Unit,
    onQuantityChange: (Int, String) -> Unit
) {
    var quantity by remember { mutableStateOf(ingredient.quantity) }
    var unit by remember { mutableStateOf(ingredient.unit) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = quantity.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { q ->
                        quantity = q
                        onQuantityChange(q, unit)
                    }
                },
                modifier = Modifier.width(70.dp),
                singleLine = true,
                label = { Text("Qty") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // unit menu
            var expanded by remember { mutableStateOf(false) }
            val units = listOf("pcs", "kg", "g", "L", "ml")

            Box {
                Button(onClick = { expanded = true }) {
                    Text(unit)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                unit = u
                                expanded = false
                                onQuantityChange(quantity, unit)
                            }
                        )
                    }
                }
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ingredient"
            )
        }
    }
}


@Composable
fun AddIngredientDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    availableIngredients: List<String>,
    onIngredientSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search ingredients...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (searchQuery.isBlank()) "Suggested:" else "Results:",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show filtered ingredients
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(availableIngredients) { ingredient ->
                        Card(
                            onClick = { onIngredientSelected(ingredient) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = ingredient,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
