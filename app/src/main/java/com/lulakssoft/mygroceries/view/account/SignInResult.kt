package com.lulakssoft.mygroceries.view.account

import com.google.firebase.auth.FirebaseUser

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?,
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?,
)

fun FirebaseUser.toUserData(): UserData =
    UserData(
        userId = uid,
        username = displayName,
        profilePictureUrl = photoUrl?.toString(), // Add null check
    )
