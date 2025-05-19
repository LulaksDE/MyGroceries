package com.lulakssoft.mygroceries.database.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class ImageBitmapConverters {
    @TypeConverter
    fun fromImageBitmap(imageBitmap: ImageBitmap): ByteArray {
        // Convert ImageBitmap to Android Bitmap to preserve pixel data
        val bitmap = imageBitmap.asAndroidBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toImageBitmap(byteArray: ByteArray): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return bitmap.asImageBitmap()
    }
}
