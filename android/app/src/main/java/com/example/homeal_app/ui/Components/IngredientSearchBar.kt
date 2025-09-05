package com.example.homeal_app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.homeal_app.model.Ingredient

/**
 * Reusable ingredient search bar component
 * Used by both Fridge and Shopping fragments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<Ingredient>,
    onSuggestionClick: (Ingredient) -> Unit,
    onAddIngredient: (String) -> Unit,
    placeholder: String = "Search ingredients...",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Close dropdown when search query is empty
    LaunchedEffect(searchQuery) {
        expanded = searchQuery.isNotBlank() && suggestions.isNotEmpty()
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                onSearchQueryChange(query)
                expanded = query.isNotBlank()
            },
            label = { Text(placeholder) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row {
                    if (searchQuery.isNotBlank()) {
                        // Add custom ingredient button
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    onAddIngredient(searchQuery)
                                    onSearchQueryChange("")
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add custom ingredient",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Clear button
                        IconButton(
                            onClick = { 
                                onSearchQueryChange("")
                                expanded = false
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        // Suggestions dropdown
        if (expanded && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSuggestionClick(suggestion)
                                    onSearchQueryChange("")
                                    expanded = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add ingredient",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}