package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.home.HouseholdSelectionView

@Composable
fun MainView(
    viewModel: MainViewModel,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val databaseApp = remember { DatabaseApp.getInstance(context) }
    val authClient = remember { GoogleAuthUiClient(context) }

    // Remember last selected household
    val selectedHousehold = remember { mutableStateOf<Household?>(null) }

    NavHost(navController = navController, startDestination = "householdSelection") {
        composable("householdSelection") {
            HouseholdSelectionView(
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
                context = context,
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
                onSignOut = onSignOut,
                onSyncProducts = {
                    viewModel.syncProducts(context)
                },
                syncing = viewModel.isSyncing,
            )
        }
    }
}
