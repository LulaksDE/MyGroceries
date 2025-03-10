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
    Household,
    Products,
    Scanner,
    SignIn,
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
                    imageVector = Icons.Default.Place,
                    contentDescription = "Products",
                )
            },
            label = { Text("Products") },
            selected = currentView == BottomBarNavigationView.Products,
            onClick = { onNavigate(BottomBarNavigationView.Products) },
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Scanner",
                )
            },
            label = { Text("Scanner") },
            selected = currentView == BottomBarNavigationView.Scanner,
            onClick = { onNavigate(BottomBarNavigationView.Scanner) },
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Sign In",
                )
            },
            label = { Text("Sign In") },
            selected = currentView == BottomBarNavigationView.SignIn,
            onClick = { onNavigate(BottomBarNavigationView.SignIn) },
        )
    }
}
