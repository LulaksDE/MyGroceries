package com.lulakssoft.mygroceries.view.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class BottomBarNavigationView {
    Home,
    Household,
    Products,
}

@Composable
fun BottomBar(
    currentView: BottomBarNavigationView,
    onNavigate: (BottomBarNavigationView) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                )
            },
            label = { Text("Home") },
            selected = currentView == BottomBarNavigationView.Home,
            onClick = { onNavigate(BottomBarNavigationView.Home) },
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Household",
                )
            },
            label = { Text("Household") },
            selected = currentView == BottomBarNavigationView.Household,
            onClick = { onNavigate(BottomBarNavigationView.Household) },
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Products",
                )
            },
            label = { Text("Products") },
            selected = currentView == BottomBarNavigationView.Products,
            onClick = { onNavigate(BottomBarNavigationView.Products) },
        )
    }
}
