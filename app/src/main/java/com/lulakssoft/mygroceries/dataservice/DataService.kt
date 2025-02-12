package com.lulakssoft.mygroceries.dataservice

import android.util.Log
import com.lulakssoft.mygroceries.dto.ProductDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel

class DataService {
    private val apiManager = ApiManager()

    suspend fun getProductDataFromBarCode(productId: String): ProductDto {
        val requestUrl = "product/$productId"
        val response =
            apiManager.jsonHttpClient
                .get(requestUrl)

        Log.d("DataService", "Response: $response")
        Log.d("DataService", "Response Body: ${response.body<String>()}")

        return response.body()
    }

    suspend fun getProductImage(productImageUrl: String): ByteArray {
        val httpResponse: HttpResponse =
            apiManager.imageHttpClient.get(productImageUrl)
        if (httpResponse.contentLength() == null) {
            throw Exception("Content length is null")
        }
        val bytes: ByteReadChannel = httpResponse.bodyAsChannel()

        val byteBufferSize = 1024 * 100
        val byteBuffer = ByteArray(httpResponse.contentLength()!!.toInt())

        var read = 0
        do {
            val currentRead = bytes.readAvailable(byteBuffer, read, byteBufferSize)
            if (currentRead > 0) {
                read += currentRead
            }
        } while (currentRead > 0)

        return byteBuffer
    }
}
