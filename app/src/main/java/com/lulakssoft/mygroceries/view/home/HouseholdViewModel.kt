package com.lulakssoft.mygroceries.view.home

import androidx.lifecycle.ViewModel
import com.lulakssoft.mygroceries.database.product.ProductRepository

class HouseholdViewModel(
    private val repository: ProductRepository,
) : ViewModel() {
}
