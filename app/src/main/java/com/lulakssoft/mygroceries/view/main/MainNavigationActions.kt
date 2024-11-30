package com.lulakssoft.mygroceries.view.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.lulakssoft.mygroceries.R
import kotlinx.serialization.Serializable


sealed interface Route {
    @Serializable data object Household : Route
    @Serializable data object Grocery : Route
}
data class MainDestinations(
    val route: Route,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int
)

class MainNavigationActions(val navController: NavHostController){

    fun navigateTo(destination: MainDestinations){
        navController.navigate(destination.route){
            popUpTo(navController.graph.findStartDestination().id){
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

val MAIN_DESTINATIONS = listOf(
    MainDestinations(
        route = Route.Household,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.tab_household
    ),
    MainDestinations(
        route = Route.Grocery,
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        iconTextId = R.string.tab_grocery
    )
)