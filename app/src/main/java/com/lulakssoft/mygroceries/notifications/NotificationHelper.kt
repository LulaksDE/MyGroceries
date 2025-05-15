package com.lulakssoft.mygroceries.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lulakssoft.mygroceries.MainActivity
import com.lulakssoft.mygroceries.R
import com.lulakssoft.mygroceries.database.product.Product

class NotificationHelper(
    private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "expiring_products_channel"
        private const val NOTIFICATION_GROUP = "expiring_products_group"
        private var notificationId = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expiring Products"
            val descriptionText = "Notifications for products about to expire"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showExpiringProductNotification(
        product: Product,
        daysLeft: Int,
    ) {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NAVIGATE_TO_PRODUCT", product.id)
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notificationText =
            if (daysLeft > 0) {
                "Expires in $daysLeft days"
            } else if (daysLeft == 0) {
                "Expires today!"
            } else {
                "Expired ${-daysLeft} days ago!"
            }

        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(product.productName)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(NOTIFICATION_GROUP)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId++, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
                e.printStackTrace()
            } catch (e: Exception) {
                // Handle other exceptions
                e.printStackTrace()
            }
        }
    }

    fun showSummaryNotification(expiringCount: Int) {
        if (expiringCount <= 1) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Products Expiring")
                .setContentText("You have $expiringCount products about to expire")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(0, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
}
