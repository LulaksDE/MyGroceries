package com.lulakssoft.mygroceries.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val code: String,
    @SerialName("generic_name")
    val name: String,
    @SerialName("image_url")
    val imageUrl: String,
)
