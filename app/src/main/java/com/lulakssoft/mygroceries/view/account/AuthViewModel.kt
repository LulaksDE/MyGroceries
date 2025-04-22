package com.lulakssoft.mygroceries.view.account

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdRepository
import com.lulakssoft.mygroceries.sync.HouseholdSyncService
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
                                profilePictureUrl = null,
                            ),
                        )
                } ?: run {
                    _authState.value = AuthState.Error("Anonyme Anmeldung fehlgeschlagen")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fehler: ${e.message}")
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
                syncAfterLogin(userData)
            }
        }

    private fun syncAfterLogin(userData: UserData) {
        viewModelScope.launch {
            try {
                val firestoreRepo = FirestoreHouseholdRepository()
                val localHouseholdRepo =
                    HouseholdRepository(
                        DatabaseApp.getInstance(googleAuthUiClient.getContext()).householdDao,
                        DatabaseApp.getInstance(googleAuthUiClient.getContext()).householdMemberDao,
                        DatabaseApp.getInstance(googleAuthUiClient.getContext()).householdInvitationDao,
                    )
                val localProductRepo =
                    ProductRepository(
                        DatabaseApp.getInstance(googleAuthUiClient.getContext()).productDao,
                    )

                val syncService =
                    HouseholdSyncService(localHouseholdRepo, localProductRepo, firestoreRepo)
                syncService.syncUserHouseholds(userData.userId)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during sync after login", e)
            }
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
