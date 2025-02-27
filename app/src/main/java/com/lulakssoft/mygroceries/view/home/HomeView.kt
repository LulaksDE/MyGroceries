package com.lulakssoft.mygroceries.view.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lulakssoft.mygroceries.R

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add Household",
                )
            }
        },
    ) { padding ->
        LazyColumn {
            items(products) { product ->
                Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Text(product.productName)
                    Text(product.productQuantity.toString())
                    Text(product.productBestBeforeDate.toString())
                }
            }
        }
    }
}
