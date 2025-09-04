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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.homeal_app.model.FridgeIngredient
import com.example.homeal_app.ui.components.IngredientSearchBar

class FridgeFragment : Fragment() {

    private val fridgeViewModel: FridgeViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

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
    val ingredients by viewModel.ingredients.observeAsState(emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState("")
    val availableIngredients by viewModel.availableIngredients.observeAsState(emptyList())
    val showAddDialog by viewModel.showAddDialog.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        
        // Header with Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Button(
                onClick = { viewModel.showDialog() }
            ) {
                Text("Add Ingredient")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Show search bar only when dialog is open
        if (showAddDialog) {
            IngredientSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                suggestions = availableIngredients,
                onSuggestionClick = { ingredient ->
                    viewModel.addIngredientFromSuggestion(ingredient)
                    viewModel.hideDialog()
                },
                onAddIngredient = { name ->
                    viewModel.addIngredient(name)
                    viewModel.hideDialog()
                },
                placeholder = "Search ingredients to add to fridge..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cancel button
            Button(
                onClick = { viewModel.hideDialog() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancel")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Ingredients list
        if (ingredients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your fridge is empty.\nTap 'Add Ingredient' to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = ingredients,
                    key = { it.id }
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
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: FridgeIngredient,
    onRemove: () -> Unit,
    onQuantityChange: (Int, String) -> Unit
) {
    var quantity: Int by remember { mutableStateOf(ingredient.quantity) }
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

            // Unit dropdown menu
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
                                onQuantityChange(quantity, u)
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
