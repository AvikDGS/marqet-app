package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CategoryData
import com.example.model.NewsCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _selectedCategoryIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("selected_categories", setOf("stock_markets", "football_soccer", "movies_ott")) ?: setOf("stock_markets", "football_soccer", "movies_ott")
    )
    val selectedCategoryIds: StateFlow<Set<String>> = _selectedCategoryIds.asStateFlow()

    private val _currentCategoryId = MutableStateFlow<String>(
        prefs.getString("current_category", "stock_markets") ?: "stock_markets"
    )
    val currentCategoryId: StateFlow<String> = _currentCategoryId.asStateFlow()

    fun toggleCategorySelection(categoryId: String) {
        val currentSet = _selectedCategoryIds.value.toMutableSet()
        if (currentSet.contains(categoryId)) {
            if (currentSet.size > 1) {
                currentSet.remove(categoryId)
            }
        } else {
            currentSet.add(categoryId)
        }
        _selectedCategoryIds.value = currentSet
        prefs.edit().putStringSet("selected_categories", currentSet).apply()
        
        // If current active category was deselected, set active to another selected
        if (!_selectedCategoryIds.value.contains(_currentCategoryId.value)) {
            val nextCurrent = _selectedCategoryIds.value.first()
            setCurrentCategory(nextCurrent)
        }
    }

    fun setCurrentCategory(categoryId: String) {
        _currentCategoryId.value = categoryId
        prefs.edit().putString("current_category", categoryId).apply()
    }
}
