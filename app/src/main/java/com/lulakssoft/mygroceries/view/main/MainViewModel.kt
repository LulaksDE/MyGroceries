package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.Household
import com.lulakssoft.mygroceries.database.HouseholdDatabase
import com.lulakssoft.mygroceries.database.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
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
