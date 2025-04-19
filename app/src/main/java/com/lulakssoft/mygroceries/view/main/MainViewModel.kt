package com.lulakssoft.mygroceries.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.Household
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import com.lulakssoft.mygroceries.database.household.MemberRole
import com.lulakssoft.mygroceries.database.product.DatabaseApp
import com.lulakssoft.mygroceries.database.product.ProductRepository
import com.lulakssoft.mygroceries.view.account.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainViewModel : ViewModel() {
    private lateinit var _productRepository: ProductRepository
    val productRepository: ProductRepository
        get() = _productRepository

    private lateinit var _householdRepository: HouseholdRepository
    val householdRepository: HouseholdRepository
        get() = _householdRepository

    var householdText by mutableStateOf("")
    var selectedHousehold by mutableStateOf(Household(0, "", ""))

    private var currentUser: UserData? = null

    // Flow für die Haushalte des aktuellen Benutzers
    private var _households = mutableStateOf<Flow<List<Household>>>(emptyFlow())
    val households: Flow<List<Household>>
        get() = _households.value

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

    fun setCurrentUser(userData: UserData) {
        currentUser = userData
        // Aktualisiere die Haushalte für den neuen Benutzer
        viewModelScope.launch {
            _households.value = householdRepository.getHouseholdsByUserId(userData.userId)
        }
    }

    fun insert() =
        viewModelScope.launch {
            // Benutze die ID des aktuellen Benutzers, wenn verfügbar
            val userId = currentUser?.userId ?: ""
            val householdId = householdRepository.insertHouseholdAndGetId(Household(0, householdText, userId))

            // Benutzer als Admin zum Haushalt hinzufügen
            householdRepository.householdDao.getHouseholdById(householdId.toInt())?.let { household ->
                val member =
                    HouseholdMember(
                        id = 0,
                        householdId = household.id,
                        userId = userId,
                        role = MemberRole.ADMIN,
                        joinedAt = LocalDateTime.now(),
                        userName = currentUser?.username ?: "Unknown",
                    )
                householdRepository.memberDao.insertMember(member)
            }
        }
}
