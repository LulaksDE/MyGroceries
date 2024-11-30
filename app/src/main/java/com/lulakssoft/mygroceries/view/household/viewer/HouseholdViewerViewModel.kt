package com.lulakssoft.mygroceries.view.household.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdDatabase
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HouseholdViewerViewModel : ViewModel() {
    private lateinit var repository: HouseholdRepository
    lateinit var households: Flow<List<Household>>
    fun initialize(database: HouseholdDatabase) {
        this.repository = HouseholdRepository(database.householdDao)
        households = repository.allHouseholds
    }

    fun delete(household: Household) =
        viewModelScope.launch {
            repository.delete(household)
        }

}