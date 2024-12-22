package com.lulakssoft.mygroceries.view.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProductsView(viewModel: ProductsViewModel) {
    Scaffold { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Text("You've clicked on the products tab!")
        }
    }
}
