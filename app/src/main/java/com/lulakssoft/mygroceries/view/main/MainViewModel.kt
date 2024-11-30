package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdDatabase
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState(loading = true))
    val uiState: StateFlow<MainViewState> = _uiState.asStateFlow()

    private lateinit var repository: HouseholdRepository

    var householdText by mutableStateOf("")
    lateinit var households: Flow<List<Household>>

    fun initialize(database: HouseholdDatabase) {
        this.repository = HouseholdRepository(database.householdDao)
        households = repository.allHouseholds
    }

    fun insert() =
        viewModelScope.launch {
            repository.insert(Household(0, householdText))
            householdText = ""
        }

    fun deleteAll() =
        viewModelScope.launch {
            repository.deleteAll()
        }

    fun delete(household: Household) =
        viewModelScope.launch {
            repository.delete(household)
        }
}

data class MainViewState(
    val households: List<Household> = emptyList(),
    val householdText: String = "",
    val expanded: Boolean = false,
    val selectedOption: String = "",
    val selectedIcon: BottomBarNavigation = BottomBarNavigation.Home,
    val loading: Boolean = false,
)
