package com.lulakssoft.mygroceries.view.household.viewer

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lulakssoft.mygroceries.database.household.Household

@Composable
fun HouseholdViewerView(
    viewModel: HouseholdViewerViewModel,
    onHouseholdSelected: (Household) -> Unit,
) {
    val households by viewModel.households.collectAsState(initial = emptyList())
    LazyColumn {
        items(households) { household ->
            HouseholdViewerItem(household, onHouseholdSelected)
        }
    }
}

@Composable
fun HouseholdViewerItem(
    household: Household,
    onHouseholdSelected: (Household) -> Unit,
) {
    Text(household.householdName)
}