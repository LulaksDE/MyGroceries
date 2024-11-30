package com.lulakssoft.mygroceries.view.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdDatabase
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var householdText by mutableStateOf("")
    private lateinit var repository: HouseholdRepository
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

}