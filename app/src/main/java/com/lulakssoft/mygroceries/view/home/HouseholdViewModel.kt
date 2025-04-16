package com.lulakssoft.mygroceries.view.home

import androidx.lifecycle.ViewModel
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

class HouseholdViewModel(
    private val productRepository: ProductRepository,
    private val authClient: GoogleAuthUiClient,
) : ViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData = _userData.asStateFlow()

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

    // Die fehlende userHouseholds-Variable
    val userHouseholds = householdRepository.getUserHouseholds() ?: emptyFlow()

    init {
        // Benutzerdaten laden
        authClient.getSignedInUser()?.let { user ->
            _userData.value = user.toUserData()
        }
    }
}
