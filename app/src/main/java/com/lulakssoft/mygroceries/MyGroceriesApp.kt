package com.lulakssoft.mygroceries

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lulakssoft.mygroceries.notifications.ExpiringProductsCheckWorker
import java.util.concurrent.TimeUnit

class MyGroceriesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupRecurringExpiryCheck()
    }

    private fun setupRecurringExpiryCheck() {
        Log.d("MyGroceriesApp", "Setting up recurring expiry check...")
        val expiryCheckRequest =
            PeriodicWorkRequestBuilder<ExpiringProductsCheckWorker>(
                15,
                TimeUnit.MINUTES,
            ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check_work",
            ExistingPeriodicWorkPolicy.KEEP,
            expiryCheckRequest,
        )
    }
}
