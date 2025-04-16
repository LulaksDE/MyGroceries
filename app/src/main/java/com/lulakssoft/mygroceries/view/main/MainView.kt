package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.Household
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.home.HouseholdSelectionScreen

@Composable
fun MainView(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val databaseApp = remember { DatabaseApp.getInstance(context) }
    val authClient = remember { GoogleAuthUiClient(context) }

    // Check if user has households
    val households by viewModel.households.collectAsState(initial = emptyList())
    val hasHouseholds = households.isNotEmpty()

    // Remember last selected household
    val selectedHousehold = rememberSaveable { mutableStateOf<Household?>(null) }

    // Route based on household selection
    LaunchedEffect(key1 = hasHouseholds) {
        if (!hasHouseholds) {
            navController.navigate("householdSelection") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else if (selectedHousehold.value == null && hasHouseholds) {
            selectedHousehold.value = households.first()
            viewModel.selectedHousehold = households.first()
        }
    }

    NavHost(navController = navController, startDestination = "householdSelection") {
        composable("householdSelection") {
            HouseholdSelectionScreen(
                onHouseholdSelected = { household ->
                    selectedHousehold.value = household
                    viewModel.selectedHousehold = household
                    navController.navigate("mainContent") {
                        popUpTo("householdSelection") { inclusive = true }
                    }
                },
                viewModel = viewModel,
                authClient = authClient,
                databaseApp = databaseApp,
            )
        }

        composable("mainContent") {
            MainContent(
                viewModel = viewModel,
                authClient = authClient,
                databaseApp = databaseApp,
                onOpenHouseholdSelection = {
                    navController.navigate("householdSelection")
                },
            )
        }
    }
}
