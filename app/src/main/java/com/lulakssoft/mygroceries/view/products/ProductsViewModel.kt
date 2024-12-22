package com.lulakssoft.mygroceries.view.products

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.dataservice.DataService
import com.lulakssoft.mygroceries.dto.ProductDto
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {
    private val dataService = DataService()

    var product: ProductDto by mutableStateOf(ProductDto("", "", ""))
    var errorMessage: String by mutableStateOf("")
    var loading: Boolean by mutableStateOf(false)
    lateinit var productImage: ImageBitmap
    var scannedCode: String by mutableStateOf("")

    fun getProduct() {
        viewModelScope.launch {
            errorMessage = ""
            loading = true

            try {
                val foundProduct = dataService.getProductDataFromBarCode(scannedCode)

                val loadedUserImage = dataService.getProductImage(foundProduct.imageUrl)
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

    private val _scannedQrCode = MutableLiveData<String>()
    val scannedQrCode: LiveData<String> get() = _scannedQrCode

    fun onQrCodeScanned(qrCode: String) {
        _scannedQrCode.value = qrCode
        // Perform additional logic like fetching product details
        Log.d("ProductsViewModel", "QR Code Scanned: $qrCode")
        getProduct()
    }
}
