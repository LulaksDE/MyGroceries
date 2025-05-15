package com.lulakssoft.mygroceries.view.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.dataservice.FirestoreHouseholdRepository
import com.lulakssoft.mygroceries.sync.HouseholdSyncService
import com.lulakssoft.mygroceries.view.account.GoogleAuthUiClient
import com.lulakssoft.mygroceries.view.account.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private lateinit var _productRepository: ProductRepository
    val productRepository: ProductRepository
        get() = _productRepository

    private lateinit var _householdRepository: HouseholdRepository
    val householdRepository: HouseholdRepository
        get() = _householdRepository

    var householdText by mutableStateOf("")
    var selectedHousehold by mutableStateOf(Household(0, "", ""))

    val TAG = "MainViewModel"

    internal var currentUser: UserData? = null

    // Flow für die Haushalte des aktuellen Benutzers
    private var _households = mutableStateOf<Flow<List<Household>>>(emptyFlow())
    val households: Flow<List<Household>>
        get() = _households.value

    private var _members = mutableStateOf<Flow<List<HouseholdMember>>>(emptyFlow())
    val members: Flow<List<HouseholdMember>>
        get() = _members.value

    fun initialize(databaseApp: DatabaseApp) {
        _productRepository =
            ProductRepository(
                databaseApp.productDao,
            )
        _householdRepository =
            HouseholdRepository(
                databaseApp.householdDao,
                databaseApp.householdMemberDao,
                databaseApp.householdInvitationDao,
            )

        // Wenn ein Benutzer gesetzt wurde, hole die Haushalte für diesen Benutzer
        currentUser?.let { user ->
            viewModelScope.launch {
                _households.value = householdRepository.getHouseholdsByUserId(user.userId)
            }
        }
    }

    var isSyncing by mutableStateOf(false)
        private set

    fun syncHouseholds(context: Context) {
        viewModelScope.launch {
            if (isSyncing) return@launch

            isSyncing = true
            try {
                currentUser?.let { user ->
                    val syncService =
                        HouseholdSyncService(
                            householdRepository,
                            productRepository,
                            FirestoreHouseholdRepository(context),
                        )
                    syncService.syncUserHouseholds(user.userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during household sync", e)
            } finally {
                isSyncing = false
            }
        }
    }

    fun syncProducts(context: Context) {
        viewModelScope.launch {
            if (isSyncing) return@launch

            isSyncing = true
            try {
                currentUser?.let { user ->
                    val syncService =
                        HouseholdSyncService(
                            householdRepository,
                            productRepository,
                            FirestoreHouseholdRepository(context),
                        )
                    syncService.syncHouseholdProducts(selectedHousehold.firestoreId.toString(), selectedHousehold.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during product sync", e)
            } finally {
                isSyncing = false
            }
        }
    }

    private var onSignOutCallback: (() -> Unit)? = null

    fun signOut(authClient: GoogleAuthUiClient) {
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

    fun setCurrentUser(userData: UserData) {
        currentUser = userData
        // Aktualisiere die Haushalte für den neuen Benutzer
        viewModelScope.launch {
            _households.value = householdRepository.getHouseholdsByUserId(userData.userId)
        }
    }

    fun insert() =
        viewModelScope.launch {
            householdRepository.createHousehold(householdText)
        }
}
