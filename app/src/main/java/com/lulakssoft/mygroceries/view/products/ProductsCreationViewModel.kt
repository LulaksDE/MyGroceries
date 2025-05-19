package com.lulakssoft.mygroceries.view.products

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ProductsCreationViewModel(
    private val repository: ProductRepository,
    private val userId: String,
) : ViewModel() {
    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private lateinit var currentHousehold: Household

    fun setCurrentHousehold(household: Household) {
        currentHousehold = household
    }

    fun saveProduct(
        name: String,
        brand: String,
        quantity: Int,
        bestBeforeDate: LocalDate,
        image: ImageBitmap?,
    ) {
        val defaultImage = ImageBitmap(1, 1)
        val productImage = image ?: defaultImage

        viewModelScope.launch {
            isSaving = true
            errorMessage = null

            try {
                val product =
                    Product(
                        id = 0,
                        householdId = currentHousehold.id,
                        firestoreId = currentHousehold.firestoreId.toString(),
                        productUuid = UUID.randomUUID().toString(),
                        creatorId = userId,
                        productName = name,
                        productBrand = brand,
                        productBarcode = "", // Empty for manually created products
                        productQuantity = quantity,
                        productBestBeforeDate = bestBeforeDate,
                        productEntryDate = LocalDateTime.now(),
                        productImage = productImage,
                        productImageUrl = "", // Empty for manually created products
                        isSynced = false,
                    )

                Log.d("ProductsCreationViewModel", "Saving product: $product")
                repository.insertProduct(product)
            } catch (e: Exception) {
                Log.e("ProductsCreationViewModel", "Error saving product: ${e.message}")
                errorMessage = "Failed to save product: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }
}
