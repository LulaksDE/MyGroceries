package com.lulakssoft.mygroceries.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val code: String,
    @SerialName("product")
    val product: ProductInfo,
)

@Serializable
data class ProductInfo(
    @SerialName("brands")
    val brand: String,
    @SerialName("product_name")
    val name: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("quantity")
    val quantity: String,
)
