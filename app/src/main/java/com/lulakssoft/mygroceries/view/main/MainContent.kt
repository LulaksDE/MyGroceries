package com.lulakssoft.mygroceries.view.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.home.HouseholdView
import com.lulakssoft.mygroceries.view.home.HouseholdViewModel
import com.lulakssoft.mygroceries.view.management.HouseholdManagementView
import com.lulakssoft.mygroceries.view.management.HouseholdManagementViewModel
import com.lulakssoft.mygroceries.view.products.ProductsView
import com.lulakssoft.mygroceries.view.products.ProductsViewModel
import com.lulakssoft.mygroceries.view.scanner.ScannerView
import com.lulakssoft.mygroceries.view.scanner.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: MainViewModel,
    authClient: GoogleAuthUiClient,
    databaseApp: DatabaseApp,
    onOpenHouseholdSelection: () -> Unit,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val householdViewModel =
        remember {
            HouseholdViewModel(viewModel.productRepository, authClient).apply {
                setOnSignOutCallback(onSignOut)
            }
        }
    val productsViewModel = remember { ProductsViewModel(viewModel.productRepository) }
    val scannerViewModel =
        remember {
            ScannerViewModel(viewModel.productRepository).apply {
                setCurrentHousehold(viewModel.selectedHousehold)
            }
        }

    val householdRepository =
        remember {
            HouseholdRepository(
                databaseApp.householdDao,
                databaseApp.householdMemberDao,
                databaseApp.householdInvitationDao,
            )
        }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.selectedHousehold.householdName) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                actions = {
                    IconButton(onClick = onOpenHouseholdSelection) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Change Household",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomBar(
                currentView =
                    when (currentRoute) {
                        "householdView" -> BottomBarNavigationView.Household
                        "productsView" -> BottomBarNavigationView.Products
                        "scannerView" -> BottomBarNavigationView.Scanner
                        else -> BottomBarNavigationView.Household
                    },
                onNavigate = { view ->
                    val targetRoute =
                        when (view) {
                            BottomBarNavigationView.Household -> "householdView"
                            BottomBarNavigationView.Products -> "productsView"
                            BottomBarNavigationView.Scanner -> "scannerView"
                        }
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "householdView",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = "householdView") {
                householdViewModel.updateSelectedHousehold(viewModel.selectedHousehold.firestoreId.toString())
                householdViewModel.updateMemberCount(viewModel.selectedHousehold.firestoreId.toString())
                HouseholdView(
                    householdViewModel,
                    navigateToManagement = {
                        navController.navigate("householdManagement")
                    },
                )
            }
            composable(route = "productsView") {
                productsViewModel.updateSelectedHousehold(viewModel.selectedHousehold)
                ProductsView(productsViewModel)
            }
            composable(route = "scannerView") {
                ScannerView(scannerViewModel)
            }
            composable(route = "householdManagement") {
                val managementViewModel =
                    viewModel {
                        HouseholdManagementViewModel(householdRepository).apply {
                            selectedHouseholdId = viewModel.selectedHousehold.id
                            selectedHouseholdFirestoreId = viewModel.selectedHousehold.firestoreId.toString()
                        }
                    }
                HouseholdManagementView(managementViewModel)
            }
        }
    }
}
