package com.lulakssoft.mygroceries.view.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
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
fun MainView(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val databaseApp = remember { DatabaseApp.getInstance(context) }

    val authClient = remember { GoogleAuthUiClient(context) }
    val householdViewModel = remember { HouseholdViewModel(viewModel.productRepository, authClient) }
    val productsViewModel = remember { ProductsViewModel(viewModel.productRepository) }
    val scannerViewModel = remember { ScannerViewModel(viewModel.productRepository) }

    val households by viewModel.households.collectAsState(initial = emptyList())
    val expanded = remember { mutableStateOf(false) }
    val selectedHousehold = rememberSaveable { mutableStateOf("") }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val householdRepository =
        remember {
            HouseholdRepository(
                viewModel.productRepository.householdDao,
                databaseApp.householdMemberDao,
                databaseApp.householdInvitationDao,
                FirestoreManager(),
            )
        }

    val householdManagementViewModel = HouseholdManagementViewModel(householdRepository)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            if (selectedHousehold.value.isEmpty()) "Bitte Haushalt wählen" else selectedHousehold.value,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { expanded.value = true },
                        )
                        IconButton(onClick = { expanded.value = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = "Dropdown Menu",
                            )
                        }
                    }
                    DropdownMenu(
                        modifier = Modifier.fillMaxWidth(),
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                    ) {
                        for (household in households) {
                            DropdownMenuItem(
                                text = { Text(household.householdName) },
                                onClick = {
                                    viewModel.selectedHousehold = household
                                    selectedHousehold.value = viewModel.selectedHousehold.householdName
                                    expanded.value = false
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = true,
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
                                popUpTo(targetRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            Button(
                onClick = { navController.navigate("householdManagementView") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text("Haushaltsverwaltung öffnen")
            }
            NavHost(navController, "householdView") {
                composable(route = "householdView") {
                    HouseholdView(householdViewModel)
                }
                composable(route = "productsView") {
                    ProductsView(productsViewModel)
                }
                composable(route = "scannerView") {
                    ScannerView(scannerViewModel)
                }
                composable(route = "householdManagementView") {
                    HouseholdManagementView(householdManagementViewModel)
                }
            }
        }
    }
}
