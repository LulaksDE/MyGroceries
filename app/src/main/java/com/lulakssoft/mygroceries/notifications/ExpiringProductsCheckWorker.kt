package com.lulakssoft.mygroceries.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ExpiringProductsCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                Log.d("ExpiringProductsCheckWorker", "Checking for expiring products...")
                val productDao = DatabaseApp.getInstance(applicationContext).productDao
                val productRepository = ProductRepository(productDao)
                val notificationHelper = NotificationHelper(applicationContext)

                // Get products expiring in next 3 days
                val expiringProducts = productRepository.getExpiringProducts(3)

                if (expiringProducts.isNotEmpty()) {
                    val today = LocalDate.now()

                    expiringProducts.forEach { product ->
                        val daysLeft = ChronoUnit.DAYS.between(today, product.productBestBeforeDate).toInt()
                        notificationHelper.showExpiringProductNotification(product, daysLeft)
                    }

                    // Show summary if multiple products are expiring
                    if (expiringProducts.size > 1) {
                        notificationHelper.showSummaryNotification(expiringProducts.size)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
}
