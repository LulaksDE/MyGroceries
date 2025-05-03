package com.lulakssoft.mygroceries.view.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductRepository
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository,
) : ViewModel() {
    var productList by mutableStateOf<List<Product>>(emptyList())

    fun updateSelectedHousehold(household: Household) {
        if (household.firestoreId.toString().isNotEmpty()) {
            getProductForHousehold(household.firestoreId.toString())
        } else {
            productList = emptyList()
        }
    }

    fun getProductForHousehold(firestoreId: String) {
        viewModelScope.launch {
            productList = repository.getProductsByFirestoreId(firestoreId)
        }
    }

    private fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun deleteSelectedProducts(products: List<Product>) {
        viewModelScope.launch {
            for (product in products) {
                deleteProduct(product)
            }
        }
    }
}
