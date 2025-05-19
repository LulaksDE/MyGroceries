package com.lulakssoft.mygroceries.view.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.lulakssoft.mygroceries.R

enum class BottomBarNavigationView {
    Household,
    Products,
    Scanner,
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
                    painter = painterResource(R.drawable.ic_products),
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
                    painter = painterResource(R.drawable.ic_barcode_scan),
                    contentDescription = "Scanner",
                )
            },
            label = { Text("Scanner") },
            selected = currentView == BottomBarNavigationView.Scanner,
            onClick = { onNavigate(BottomBarNavigationView.Scanner) },
        )
    }
}
