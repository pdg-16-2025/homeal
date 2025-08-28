package com.example.homeal_app.ui.Calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class CalendarViewModel : ViewModel() {

    private val savedMeals = mutableMapOf<LocalDate, Pair<String, String>>()

    private val _weekDays = MutableLiveData<List<DayData>>()
    val weekDays: LiveData<List<DayData>> = _weekDays

    private val _currentWeekTitle = MutableLiveData<String>()
    val currentWeekTitle: LiveData<String> = _currentWeekTitle

    private var currentMonday: LocalDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

    init {
        loadCurrentWeek()
    }

    private fun loadCurrentWeek() {
        val days = (0..6).map { offset ->
            val date = currentMonday.plusDays(offset.toLong())
            val meals = savedMeals[date] ?: Pair("", "")
            DayData(
                date = date,
                lunchMeal = meals.first,
                dinnerMeal = meals.second
            )
        }
        _weekDays.value = days

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

    // Link recettes choisies et les mettre dans la db
    //GET recipe Ingredient
    //Add to shopping list
    fun addMeal(date: LocalDate, mealType: String, meal: String) {
        val existing = savedMeals[date] ?: Pair("", "")

        savedMeals[date] = when (mealType) {
            "Lunch" -> existing.copy(first = meal)
            "Dinner" -> existing.copy(second = meal)
            else -> existing
        }

        loadCurrentWeek()
    }
    fun removeMeal(date: LocalDate, mealType: String) {
        val existing = savedMeals[date] ?: Pair("", "")

        savedMeals[date] = when (mealType) {
            "Lunch" -> existing.copy(first = "")
            "Dinner" -> existing.copy(second = "")
            else -> existing
        }

        loadCurrentWeek()
    }

    fun getMeal(date: LocalDate, mealType: String): String {
        val meals = savedMeals[date] ?: return ""
        return when (mealType) {
            "Lunch" -> meals.first
            "Dinner" -> meals.second
            else -> ""
        }
    }

    fun cookMeal(){

    }

}
