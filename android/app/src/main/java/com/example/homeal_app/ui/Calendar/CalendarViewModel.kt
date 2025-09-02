package com.example.homeal_app.ui.Calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeal_app.data.repository.CalendarRepository
import com.example.homeal_app.data.local.database.AppDatabase
import com.example.homeal_app.data.remote.NetworkModule
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DayData(
    val date: LocalDate,
    val lunchMeal: String,
    val dinnerMeal: String
) {
    val dayName: String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayNumber: String = date.format(DateTimeFormatter.ofPattern("d"))
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize repository with database and API service
    private val repository = CalendarRepository(
        AppDatabase.getDatabase(application).plannedMealDao(),
        NetworkModule.apiService
    )

    private val _weekDays = MutableLiveData<List<DayData>>()
    val weekDays: LiveData<List<DayData>> = _weekDays

    private val _currentWeekTitle = MutableLiveData<String>()
    val currentWeekTitle: LiveData<String> = _currentWeekTitle

    private var currentMonday: LocalDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

    init {
        loadCurrentWeek()
    }

    private fun loadCurrentWeek() {
        viewModelScope.launch {
            try {
                val startDate = currentMonday.toString()
                val endDate = currentMonday.plusDays(6).toString()

                // Get planned meals from database
                repository.getMealsForWeek(startDate, endDate).collect { plannedMeals ->
                    val days = (0..6).map { offset ->
                        val date = currentMonday.plusDays(offset.toLong())
                        val dateStr = date.toString()

                        val lunchMeal = plannedMeals.find {
                            it.mealDate == dateStr && it.mealType == "Lunch"
                        }?.recipeName ?: ""

                        val dinnerMeal = plannedMeals.find {
                            it.mealDate == dateStr && it.mealType == "Dinner"
                        }?.recipeName ?: ""

                        DayData(
                            date = date,
                            lunchMeal = lunchMeal,
                            dinnerMeal = dinnerMeal
                        )
                    }
                    _weekDays.value = days
                }
            } catch (e: Exception) {
                // Fallback to empty days on error
                val days = (0..6).map { offset ->
                    val date = currentMonday.plusDays(offset.toLong())
                    DayData(date = date, lunchMeal = "", dinnerMeal = "")
                }
                _weekDays.value = days
            }
        }

        val start = currentMonday.format(DateTimeFormatter.ofPattern("MMM d"))
        val end = currentMonday.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d"))
        _currentWeekTitle.value = "Week of $start - $end"
    }

    fun goToPreviousWeek() {
        currentMonday = currentMonday.minusWeeks(1)
        loadCurrentWeek()
    }

    fun goToNextWeek() {
        currentMonday = currentMonday.plusWeeks(1)
        loadCurrentWeek()
    }

    // Updated to save in database
    fun addMeal(date: LocalDate, mealType: String, meal: String) {
        viewModelScope.launch {
            try {
                // For now, we use a placeholder recipe ID (0)
                // TODO: Get actual recipe ID from meal selection
                repository.addMealToCalendar(
                    recipeId = 0,
                    recipeName = meal,
                    date = date.toString(),
                    mealType = mealType
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeMeal(date: LocalDate, mealType: String) {
        viewModelScope.launch {
            try {
                repository.replaceMeal(
                    date = date.toString(),
                    mealType = mealType,
                    newRecipeId = 0,
                    newRecipeName = ""
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMeal(date: LocalDate, mealType: String): String {
        // This will be handled by the database observation in loadCurrentWeek
        return ""
    }

    fun cookMeal() {
        // TODO: Implementation when cooking feature is added
    }
}
