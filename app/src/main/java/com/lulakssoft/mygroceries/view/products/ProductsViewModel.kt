package com.lulakssoft.mygroceries.view.products

import androidx.lifecycle.ViewModel
import com.lulakssoft.mygroceries.database.product.ProductRepository

class ProductsViewModel(
    private val repository: ProductRepository,
) : ViewModel() {
    val products = repository.allProducts
}
