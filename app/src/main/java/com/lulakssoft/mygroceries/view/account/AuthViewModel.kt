package com.lulakssoft.mygroceries.view.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()
    var loading: Boolean by mutableStateOf(false)
    var errorMessage: String by mutableStateOf("")

    private val auth = Firebase.auth

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signInAnonymously() =
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInAnonymously().await()
                result.user?.let { user ->
                    _authState.value =
                        AuthState.Authenticated(
                            UserData(
                                userId = user.uid,
                                username = "Guest",
                                profilePictureUrl = "",
                                email = "",
                            ),
                        )
                } ?: run {
                    _authState.value = AuthState.Error("Anonymous sign-in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }

    fun signIn() =
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val signInResult = googleAuthUiClient.signIn()

            // Ensure we're updating state properly
            _authState.value =
                if (signInResult.data != null) {
                    AuthState.Authenticated(signInResult.data)
                } else {
                    AuthState.Error(signInResult.errorMessage ?: "Unknown error")
                }
            if (_authState.value is AuthState.Authenticated) {
                val userData = (_authState.value as AuthState.Authenticated).userData
            }
        }

    init {
        // Check if already signed in
        googleAuthUiClient.getSignedInUser()?.let { user ->
            _authState.value = AuthState.Authenticated(user.toUserData())
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()

    object Loading : AuthState()

    data class Authenticated(
        val userData: UserData,
    ) : AuthState()

    data class Error(
        val message: String,
    ) : AuthState()
}
