package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdDatabase
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductDatabase
import com.lulakssoft.mygroceries.database.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private lateinit var householdRepository: HouseholdRepository
    private lateinit var productRepository: ProductRepository

    var householdText by mutableStateOf("Toller Haushalt")
    lateinit var households: Flow<List<Household>>
    lateinit var products: Flow<List<Product>>

    fun initialize(
        householdDatabase: HouseholdDatabase,
        productDatabase: ProductDatabase,
    ) {
        this.householdRepository = HouseholdRepository(householdDatabase.householdDao)
        this.productRepository = ProductRepository(productDatabase.productDao)
        households = householdRepository.allHouseholds
        products = productRepository.allProducts
    }

    fun insert() =
        viewModelScope.launch {
            householdRepository.insert(Household(0, householdText))
            householdText = ""
        }

    fun deleteAll() =
        viewModelScope.launch {
            householdRepository.deleteAll()
        }

    fun delete(household: Household) =
        viewModelScope.launch {
            householdRepository.delete(household)
        }
}
