package com.lulakssoft.mygroceries.view.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.account.UserData
import com.lulakssoft.mygroceries.view.account.toUserData
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val productRepository: ProductRepository,
    private val authClient: GoogleAuthUiClient,
) : ViewModel() {
    var memberCount by mutableIntStateOf(0)
    var userData by mutableStateOf<UserData?>(null)
    var currentMemberRole by mutableStateOf<HouseholdMember?>(null)
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

    // Methode zur Prüfung der Mitgliedsrolle im aktuellen Haushalt
    fun loadUserRoleInHousehold(firestoreId: String) {
        viewModelScope.launch {
            authClient.getSignedInUser()?.let { user ->
                householdRepository.getUserMembershipInHousehold(firestoreId, user.uid)?.let { member ->
                    currentMemberRole = member
                }
            }
        }
    }

    fun updateMemberCount(firestoreId: String) {
        viewModelScope.launch {
            householdRepository.getMemberCountForHousehold(firestoreId).let { count ->
                memberCount = count
            }
        }
    }

    init {
        // Benutzerdaten laden
        authClient.getSignedInUser()?.let { user ->
            userData = user.toUserData()
        }
    }
}
