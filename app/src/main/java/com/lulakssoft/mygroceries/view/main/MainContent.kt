package com.lulakssoft.mygroceries.view.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.home.HouseholdView
import com.lulakssoft.mygroceries.view.home.HouseholdViewModel
import com.lulakssoft.mygroceries.view.management.HouseholdManagementView
import com.lulakssoft.mygroceries.view.management.HouseholdManagementViewModel
import com.lulakssoft.mygroceries.view.products.ProductDetailView
import com.lulakssoft.mygroceries.view.products.ProductsCreationView
import com.lulakssoft.mygroceries.view.products.ProductsCreationViewModel
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
    onSyncProducts: () -> Unit,
    syncing: Boolean,
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
    val productsCreationViewModel =
        remember { ProductsCreationViewModel(viewModel.productRepository, viewModel.currentUser?.userId.toString()) }
    val scannerViewModel =
        remember {
            ScannerViewModel(viewModel.productRepository).apply {
                setCurrentHousehold(viewModel.selectedHousehold)
                setUserId(viewModel.currentUser?.userId.toString())
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
                modifier = Modifier.clip(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                actions = {
                    IconButton(onClick = onOpenHouseholdSelection) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_household),
                            contentDescription = "Change Household",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible =
                    currentRoute != "householdManagement" &&
                        currentRoute != "productsCreation" &&
                        currentRoute != "productDetail/{productId}",
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
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
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "householdView",
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() - 12.dp),
        ) {
            composable(route = "householdView") {
                householdViewModel.updateSelectedHousehold(viewModel.selectedHousehold.firestoreId.toString())
                householdViewModel.updateMemberCount(viewModel.selectedHousehold.firestoreId.toString())
                householdViewModel.updateProductCount(viewModel.selectedHousehold.firestoreId.toString())
                householdViewModel.updateActivityList(viewModel.selectedHousehold.firestoreId.toString())
                HouseholdView(
                    householdViewModel,
                    navigateToManagement = {
                        navController.navigate("householdManagement") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(route = "productsView") {
                productsViewModel.updateSelectedHousehold(viewModel.selectedHousehold)
                ProductsView(
                    productsViewModel,
                    onSyncProducts,
                    onNavigateToCreation = {
                        navController.navigate("productsCreation") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDetails = { productId ->
                        navController.navigate("productDetail/$productId") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    syncing,
                )
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
            composable(route = "productsCreation") {
                productsCreationViewModel.setCurrentHousehold(viewModel.selectedHousehold)
                ProductsCreationView(productsCreationViewModel) {
                    navController.navigate("productsView") {
                        popUpTo("productsCreation") { inclusive = true }
                    }
                }
            }
            composable(
                route = "productDetail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                if (productId != null) {
                    ProductDetailView(
                        viewModel = productsViewModel,
                        productId = productId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}
