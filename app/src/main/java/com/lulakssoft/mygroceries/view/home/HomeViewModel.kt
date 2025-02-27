package com.lulakssoft.mygroceries.view.home

import androidx.lifecycle.ViewModel
import com.lulakssoft.mygroceries.database.product.ProductRepository

class HomeViewModel(
    private val repository: ProductRepository,
) : ViewModel() {
    val products = repository.allProducts
}
