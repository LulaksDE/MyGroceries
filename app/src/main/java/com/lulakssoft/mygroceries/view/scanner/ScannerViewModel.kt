package com.lulakssoft.mygroceries.view.scanner

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.product.Product
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.dataservice.DataService
import com.lulakssoft.mygroceries.dto.ProductDto
import com.lulakssoft.mygroceries.dto.ProductInfo
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ScannerViewModel(
    private val repository: ProductRepository,
) : ViewModel() {
    private val dataService = DataService()

    var errorMessage: String by mutableStateOf("")
    var loading: Boolean by mutableStateOf(false)

    var scannedSomething: Boolean by mutableStateOf(false)
    var scannedCode: String by mutableStateOf("")

    var product: ProductDto by mutableStateOf(ProductDto("", ProductInfo("", "", "")))
    var productImage: ImageBitmap by mutableStateOf(ImageBitmap(1, 1))
    var productBestBefore by mutableStateOf(LocalDate.now())
    var productEntryDate by mutableStateOf(LocalDateTime.now())
    var productQuantity by mutableStateOf(1)

    private lateinit var currentHousehold: Household

    fun setCurrentHousehold(household: Household) {
        currentHousehold = household
    }

    private lateinit var userId: String

    fun setUserId(id: String) {
        userId = id
    }

    fun getProduct(barcode: String) {
        viewModelScope.launch {
            errorMessage = ""
            loading = true
            productImage = ImageBitmap(1, 1)
            product = ProductDto("", ProductInfo("", "", ""))

            try {
                val foundProduct = dataService.getProductDataFromBarCode(barcode)
                Log.d("ScannerViewModel", "Product found: $foundProduct")

                val loadedUserImage = dataService.getProductImage(foundProduct.product.imageUrl)
                val bitmap = BitmapFactory.decodeByteArray(loadedUserImage, 0, loadedUserImage.size)
                productImage = bitmap.asImageBitmap()

                product = foundProduct
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "Error fetching product data: ${e.message}", e)
                errorMessage = "Scanned product not found.\nPlease scan again."
            } finally {
                loading = false
            }
        }
    }

    fun onQrCodeScanned(qrCode: String) {
        scannedCode = qrCode
        Log.d("ScannerViewModel", "QR Code Scanned: $scannedCode")
        scannedSomething = true
        getProduct(scannedCode)
    }

    fun insert() =
        viewModelScope.launch {
            if (product.product.imageUrl.isEmpty()) {
                errorMessage = "Product information is incomplete, or missing in Database.\nPlease scan again."
                return@launch
            }
            repository.insertProduct(
                Product(
                    0,
                    currentHousehold.id, // Dynamische Haushalt-ID verwenden
                    currentHousehold.firestoreId.toString(),
                    UUID.randomUUID().toString(),
                    userId,
                    product.product.name,
                    product.product.brand,
                    scannedCode,
                    productQuantity,
                    productBestBefore,
                    productEntryDate,
                    productImage,
                    product.product.imageUrl,
                ),
            )
        }
}
