package com.lulakssoft.mygroceries.dataservice

import java.time.LocalDateTime

data class FirestoreActivity(
    val activityId: String,
    val userId: String,
    val userName: String,
    val activityType: String,
    val details: String,
    val timestamp: String = LocalDateTime.now().toString(),
)
