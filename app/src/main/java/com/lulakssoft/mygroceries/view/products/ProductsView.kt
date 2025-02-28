package com.lulakssoft.mygroceries.view.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
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
fun ProductsView(viewModel: ProductsViewModel) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) { Text("You're on the products tab!") }
        LazyColumn{
            items(products) { product ->
                Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Text(product.productName)
                    Image(bitmap = product.productImage, "productImage")
                    Text("Picture Information: ${product.productImage.width}x${product.productImage.height}")
                    Text(product.productQuantity.toString())
                    Text(product.productBestBeforeDate.toString())
                }
            }
        }
    }
}
