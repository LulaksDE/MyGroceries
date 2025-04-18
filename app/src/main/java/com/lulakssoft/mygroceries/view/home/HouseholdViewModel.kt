package com.lulakssoft.mygroceries.view.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
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

    // Hier die Firebase-Firestore-Instanz erstellen
    private val firestoreManager = FirestoreManager()

    // Den HouseholdRepository initialisieren
    private val householdRepository by lazy {
        // Hole eine Instanz der Datenbank
        val databaseApp = DatabaseApp.getInstance(authClient.getContext())
        HouseholdRepository(
            productRepository.householdDao,
            databaseApp.householdMemberDao,
            databaseApp.householdInvitationDao,
            firestoreManager,
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

    fun updateSelectedHousehold(householdId: Int) {
        loadUserRoleInHousehold(householdId)
    }

    private val _currentMemberRole = MutableStateFlow<HouseholdMember?>(null)
    val currentMemberRole = _currentMemberRole.asStateFlow()

    // Methode zur Prüfung der Mitgliedsrolle im aktuellen Haushalt
    fun loadUserRoleInHousehold(householdId: Int) {
        viewModelScope.launch {
            authClient.getSignedInUser()?.let { user ->
                householdRepository.getUserMembershipInHousehold(householdId, user.uid)?.let { member ->
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
