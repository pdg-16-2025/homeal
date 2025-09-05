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
    val dinnerMeal: String,
    val lunchRecipeId: Int = 0,
    val dinnerRecipeId: Int = 0
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
        android.util.Log.d("CalendarViewModel", "loadCurrentWeek called for week starting: $currentMonday")
        viewModelScope.launch {
            try {
                val startDate = currentMonday.toString()
                val endDate = currentMonday.plusDays(6).toString()
                android.util.Log.d("CalendarViewModel", "Loading meals for week: $startDate to $endDate")

                // Get planned meals from database
                repository.getMealsForWeek(startDate, endDate).collect { plannedMeals ->
                    android.util.Log.d("CalendarViewModel", "Received ${plannedMeals.size} planned meals from database:")
                    plannedMeals.forEach { meal ->
                        android.util.Log.d("CalendarViewModel", "  - ${meal.mealDate} ${meal.mealType}: ${meal.recipeName} (ID: ${meal.recipeId})")
                    }

                    val days = (0..6).map { offset ->
                        val date = currentMonday.plusDays(offset.toLong())
                        val dateStr = date.toString()

                        val lunchMealData = plannedMeals.find {
                            it.mealDate == dateStr && it.mealType == "Lunch"
                        }
                        val lunchMeal = lunchMealData?.recipeName ?: ""
                        val lunchRecipeId = lunchMealData?.recipeId ?: 0

                        val dinnerMealData = plannedMeals.find {
                            it.mealDate == dateStr && it.mealType == "Dinner"
                        }
                        val dinnerMeal = dinnerMealData?.recipeName ?: ""
                        val dinnerRecipeId = dinnerMealData?.recipeId ?: 0

                        android.util.Log.d("CalendarViewModel", "Day $dateStr: Lunch='$lunchMeal' (ID: $lunchRecipeId), Dinner='$dinnerMeal' (ID: $dinnerRecipeId)")

                        DayData(
                            date = date,
                            lunchMeal = lunchMeal,
                            dinnerMeal = dinnerMeal,
                            lunchRecipeId = lunchRecipeId,
                            dinnerRecipeId = dinnerRecipeId
                        )
                    }
                    _weekDays.value = days
                    android.util.Log.d("CalendarViewModel", "Updated weekDays with ${days.size} days")
                }
            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "Error loading current week: ${e.message}", e)
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

    // Updated to save in database with actual recipe ID
    fun addMeal(date: LocalDate, mealType: String, recipeId: Int, recipeName: String) {
        android.util.Log.d("CalendarViewModel", "addMeal called: date=$date, mealType=$mealType, recipeId=$recipeId, recipeName=$recipeName")
        viewModelScope.launch {
            try {
                android.util.Log.d("CalendarViewModel", "About to call repository.addMealToCalendar with recipeId=$recipeId")
                repository.addMealToCalendar(
                    recipeId = recipeId,
                    recipeName = recipeName,
                    date = date.toString(),
                    mealType = mealType
                )
                android.util.Log.d("CalendarViewModel", "Successfully added meal to repository")
            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "Error adding meal: ${e.message}", e)
            }
        }
    }

    fun removeMeal(date: LocalDate, mealType: String) {
        android.util.Log.d("CalendarViewModel", "removeMeal called: date=$date, mealType=$mealType")
        viewModelScope.launch {
            try {
                android.util.Log.d("CalendarViewModel", "About to call repository.removeMealByDateAndType for proper deletion")
                repository.removeMealByDateAndType(
                    date = date.toString(),
                    mealType = mealType
                )
                android.util.Log.d("CalendarViewModel", "Successfully removed meal from repository")
            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "Error removing meal: ${e.message}", e)
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
