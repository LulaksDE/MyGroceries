package com.lulakssoft.mygroceries.view.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authClient: GoogleAuthUiClient,
) : ViewModel() {
    var user by mutableStateOf<FirebaseUser?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        user = authClient.getSignedInUser()
    }

    fun signIn() {
        println("Signing in")
        viewModelScope.launch {
            try {
                val result = authClient.signIn()
                if (result.data != null) {
                    checkCurrentUser() // Refresh the user
                    errorMessage = null
                } else {
                    errorMessage = result.errorMessage ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authClient.signOut()
                user = null
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Logout failed"
            }
        }
    }
}
