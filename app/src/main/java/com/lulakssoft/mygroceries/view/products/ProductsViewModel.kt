package com.lulakssoft.mygroceries.view.products

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.dataservice.DataService
import com.lulakssoft.mygroceries.dto.ProductDto
import com.lulakssoft.mygroceries.dto.ProductInfo
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {
    private val dataService = DataService()

    var product: ProductDto by mutableStateOf(ProductDto("", ProductInfo("", "", "")))
    var errorMessage: String by mutableStateOf("")
    var loading: Boolean by mutableStateOf(false)
    lateinit var productImage: ImageBitmap
    var scannedCode: String by mutableStateOf("")

    fun getProduct(barcode: String) {
        viewModelScope.launch {
            errorMessage = ""
            loading = true

            try {
                val foundProduct = dataService.getProductDataFromBarCode(barcode)
                Log.d("ProductsViewModel", "Product found: $foundProduct")

                val loadedUserImage = dataService.getProductImage(foundProduct.product.imageUrl)
                val bitmap = BitmapFactory.decodeByteArray(loadedUserImage, 0, loadedUserImage.size)
                productImage = bitmap.asImageBitmap()

                product = foundProduct
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            } finally {
                loading = false
            }
        }
    }

    fun onQrCodeScanned(qrCode: String) {
        scannedCode = qrCode
        // Perform additional logic like fetching product details
        Log.d("ProductsViewModel", "QR Code Scanned: $scannedCode")
        getProduct(scannedCode)
    }
}
