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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.view.home.HomeView
import com.lulakssoft.mygroceries.view.home.HomeViewModel
import com.lulakssoft.mygroceries.view.household.HouseholdView
import com.lulakssoft.mygroceries.view.household.HouseholdViewModel
import com.lulakssoft.mygroceries.view.products.ProductsView
import com.lulakssoft.mygroceries.view.products.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val homeViewModel = remember { HomeViewModel() }
    val householdViewModel = remember { HouseholdViewModel() }
    val productsViewModel = remember { ProductsViewModel() }
    val households by viewModel.households.collectAsState(initial = emptyList())
    val expanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf("") }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

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
                            if (selectedOption.value.isEmpty()) "Bitte Haushalt wählen" else selectedOption.value,
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
                                    selectedOption.value = household.householdName
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
                visible = selectedOption.value.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                BottomBar(
                    currentView =
                        when (currentRoute) {
                            "homeView" -> BottomBarNavigationView.Home
                            "householdView" -> BottomBarNavigationView.Household
                            "productsView" -> BottomBarNavigationView.Products
                            else -> BottomBarNavigationView.Home
                        },
                    onNavigate = { view ->
                        val targetRoute =
                            when (view) {
                                BottomBarNavigationView.Home -> "homeView"
                                BottomBarNavigationView.Household -> "householdView"
                                BottomBarNavigationView.Products -> "productsView"
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
            if (selectedOption.value.isEmpty()) {
                Text(
                    "Bitte wählen Sie einen Haushalt aus!",
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                )
                Button(
                    onClick = { viewModel.insert() },
                    modifier =
                        Modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally),
                ) { Text("Haushalt hinzufügen") }
            } else {
                NavHost(navController, "homeView") {
                    composable(route = "homeView") {
                        HomeView(homeViewModel)
                    }
                    composable(route = "householdView") {
                        HouseholdView(householdViewModel)
                    }
                    composable(route = "productsView") {
                        ProductsView(productsViewModel)
                    }
                }
            }
        }
    }
}
