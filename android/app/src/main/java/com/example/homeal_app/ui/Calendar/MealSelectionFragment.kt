// MealSelectionFragment.kt
package com.example.homeal_app.ui.Calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MealSelectionArgs(
    val date: LocalDate,
    val mealType: String
)

class MealSelectionFragment : Fragment() {

    // Shared ViewModel with CalendarFragment
    private val calendarViewModel: CalendarViewModel by activityViewModels()

    // Local ViewModel only for search/recommendations
    private val mealSelectionViewModel: MealSelectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get nav args
        val args = arguments?.let {
            MealSelectionArgs(
                date = LocalDate.parse(it.getString("date")),
                mealType = it.getString("mealType") ?: ""
            )
        } ?: return ComposeView(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    MealSelectionScreen(
                        args = args,
                        calendarViewModel = calendarViewModel,
                        mealSelectionViewModel = mealSelectionViewModel,
                        onMealSelected = { meal ->
                            calendarViewModel.addMeal(args.date, args.mealType, meal)
                            findNavController().popBackStack()
                        },
                        onBackPressed = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSelectionScreen(
    args: MealSelectionArgs,
    calendarViewModel: CalendarViewModel,
    mealSelectionViewModel: MealSelectionViewModel,
    onMealSelected: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    val searchQuery by mealSelectionViewModel.searchQuery.observeAsState("")
    val filteredMeals by mealSelectionViewModel.filteredMeals.observeAsState(emptyList())
    val recommendations by mealSelectionViewModel.recommendations.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Choose ${args.mealType}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = args.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { mealSelectionViewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for meals...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            if (searchQuery.isEmpty()) {
                // Print recommendations if no research
                item {
                    Text(
                        text = "Recommended for you",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                items(recommendations) { meal ->
                    MealItem(
                        meal = meal,
                        isRecommended = true,
                        onClick = { onMealSelected(meal.name) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Separator
                if (recommendations.isNotEmpty()) {
                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "All meals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            } else {
                // Print search results
                item {
                    Text(
                        text = "Search results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Meal list
            val mealsToShow = if (searchQuery.isEmpty()) {
                mealSelectionViewModel.allMeals.value ?: emptyList()
            } else {
                filteredMeals
            }

            items(mealsToShow) { meal ->
                MealItem(
                    meal = meal,
                    isRecommended = false,
                    onClick = { onMealSelected(meal.name) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Message si aucun résultat
            if (searchQuery.isNotEmpty() && filteredMeals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No meals found for \"$searchQuery\"",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealItem(
    meal: MealData,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (meal.description.isNotEmpty()) {
                    Text(
                        text = meal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Tags
                if (meal.prepTime.isNotEmpty() || meal.difficulty.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (meal.prepTime.isNotEmpty()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(meal.prepTime, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                        if (meal.difficulty.isNotEmpty()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(meal.difficulty, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            if (isRecommended) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Recommended",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}