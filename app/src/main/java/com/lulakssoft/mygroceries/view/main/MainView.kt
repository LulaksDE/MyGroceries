package com.lulakssoft.mygroceries.view.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    mainViewState: MainViewState,
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        MainNavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface {
        MainNavHost(
            navController = navController,
            mainViewState = mainViewState,
        )
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    mainViewState: MainViewState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Household,
    ) {
        composable<Route.Household> {
            MainHouseholdScreen(
                replyHomeUIState = mainViewState,
            )
        }
        composable<Route.Grocery> {
            EmptyComingSoon()
        }
    }
}
