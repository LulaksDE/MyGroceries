package com.lulakssoft.mygroceries.view.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.account.UserData
import com.lulakssoft.mygroceries.view.account.toUserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val productRepository: ProductRepository,
    private val authClient: GoogleAuthUiClient,
) : ViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData = _userData.asStateFlow()
    private var onSignOutCallback: (() -> Unit)? = null

    private val householdRepository by lazy {
        val databaseApp = DatabaseApp.getInstance(authClient.getContext())
        HouseholdRepository(
            databaseApp.householdDao,
            databaseApp.householdMemberDao,
            databaseApp.householdInvitationDao,
        )
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authClient.signOut()
                onSignOutCallback?.invoke()
            } catch (e: Exception) {
                Log.e("HouseholdViewModel", "Error signing out", e)
            }
        }
    }

    fun setOnSignOutCallback(callback: () -> Unit) {
        onSignOutCallback = callback
    }

    fun updateSelectedHousehold(firestoreId: String) {
        if (firestoreId.isNotEmpty()) {
            loadUserRoleInHousehold(firestoreId)
        } else {
            Log.d("HouseholdViewModel", "FirestoreId is empty, cannot load user role")
        }
    }

    private val _currentMemberRole = MutableStateFlow<HouseholdMember?>(null)
    val currentMemberRole = _currentMemberRole.asStateFlow()

    // Methode zur Prüfung der Mitgliedsrolle im aktuellen Haushalt
    fun loadUserRoleInHousehold(firestoreId: String) {
        viewModelScope.launch {
            authClient.getSignedInUser()?.let { user ->
                householdRepository.getUserMembershipInHousehold(firestoreId, user.uid)?.let { member ->
                    _currentMemberRole.value = member
                }
            }
        }
    }

    // Die fehlende userHouseholds-Variable
    val userHouseholds = householdRepository.getUserHouseholds() ?: emptyFlow()

    init {
        // Benutzerdaten laden
        authClient.getSignedInUser()?.let { user ->
            _userData.value = user.toUserData()
        }
    }
}
