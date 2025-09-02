package com.example.homeal_app.ui.Calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.time.LocalDate
import com.example.homeal_app.R


class CalendarFragment : Fragment() {

    private val calendarViewModel: CalendarViewModel by activityViewModels { 
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        onMealClick = { date, mealType ->
                            navigateToMealSelection(date, mealType)
                        }
                    )
                }
            }
        }
    }

    private fun navigateToMealSelection(date: LocalDate, mealType: String) {
        val bundle = Bundle().apply {
            putString("date", date.toString())
            putString("mealType", mealType)
        }

        findNavController().navigate(R.id.navigation_meal_selection, bundle)


    }
}

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onMealClick: (LocalDate, String) -> Unit
) {
    val weekDays by viewModel.weekDays.observeAsState(emptyList())
    val weekTitle by viewModel.currentWeekTitle.observeAsState("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = weekTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.goToPreviousWeek() }) {
                Text("← Prev. week")
            }

            Button(onClick = { viewModel.goToNextWeek() }) {
                Text("Next week →")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(weekDays) { day ->
                DayCard(
                    day = day,
                    onMealClick = { mealType ->
                        onMealClick(day.date, mealType)
                    },
                    onMealRemove = { mealType ->
                        viewModel.removeMeal(day.date, mealType)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DayCard(
    day: DayData,
    onMealClick: (String) -> Unit,
    onMealRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = day.dayNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MealSlot(
                label = "Lunch",
                meal = day.lunchMeal,
                onClick = { onMealClick("Lunch") },
                onRemove = { onMealRemove("Lunch") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            MealSlot(
                label = "Dinner",
                meal = day.dinnerMeal,
                onClick = { onMealClick("Dinner") },
                onRemove = { onMealRemove("Dinner") }
            )
        }
    }
}

@Composable
fun MealSlot(
    label: String,
    meal: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (meal.isEmpty())
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (meal.isEmpty()) "Choose a meal" else meal,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // remove meal
            if (meal.isNotEmpty()) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove $label",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}