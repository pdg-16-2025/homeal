package com.example.homeal_app.ui.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class ShoppingFragment : Fragment() {

    private val shoppingViewModel: ShoppingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ShoppingScreen(viewModel = shoppingViewModel)
                }
            }
        }
    }
}

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {
    val ingredients by viewModel.ingredients.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping List",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Delete all marked items button
                val hasMarkedItems = ingredients.any { it.isDone }
                if (hasMarkedItems) {
                    IconButton(
                        onClick = { viewModel.removeAllMarkedIngredients() }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Delete all marked items",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add ingredient")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ingredients) { ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    onToggleDone = { viewModel.toggleIngredientDone(ingredient.id) },
                    onRemove = { viewModel.removeIngredient(ingredient.id) }
                )
            }
        }
    }

    // Add ingredient dialog
    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, quantity ->
                viewModel.addIngredient(name, quantity)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun IngredientItem(
    ingredient: ShoppingIngredient,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = ingredient.isDone,
                onCheckedChange = { onToggleDone() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (ingredient.isDone) TextDecoration.LineThrough else null,
                    color = if (ingredient.isDone) Color.Gray else Color.Unspecified
                )
                Text(
                    text = ingredient.quantity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ingredient.isDone) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (ingredient.isDone) TextDecoration.LineThrough else null
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ingredient",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Ingredient name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientQuantity,
                    onValueChange = { ingredientQuantity = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (ingredientName.isNotBlank() && ingredientQuantity.isNotBlank()) {
                        onAdd(ingredientName, ingredientQuantity)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

