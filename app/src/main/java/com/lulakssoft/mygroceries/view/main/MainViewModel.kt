package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.Household
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private lateinit var productRepository: ProductRepository

    var householdText by mutableStateOf("Toller Haushalt")
    lateinit var households: Flow<List<Household>>
    lateinit var products: Flow<List<Product>>

    fun initialize(databaseApp: DatabaseApp) {
        this.productRepository = ProductRepository(databaseApp.productDao, databaseApp.householdDao)
        households = productRepository.allHouseholds
        products = productRepository.allProducts
    }

    fun insert() =
        viewModelScope.launch {
            productRepository.insertHousehold(Household(0, householdText))
            householdText = ""
        }

    fun deleteAll() =
        viewModelScope.launch {
            productRepository.deleteAll()
        }

    fun delete(household: Household) =
        viewModelScope.launch {
            productRepository.deleteHousehold(household)
        }
}
