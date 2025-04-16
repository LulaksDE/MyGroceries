package com.lulakssoft.mygroceries.view.management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lulakssoft.mygroceries.database.household.HouseholdMember
import com.lulakssoft.mygroceries.database.household.HouseholdRepository
import kotlinx.coroutines.launch

enum class JoinResult {
    Success,
    Failed,
}

class HouseholdManagementViewModel(
    private val repository: HouseholdRepository,
) : ViewModel() {
    var invitationCode by mutableStateOf("")
    var joinHouseholdResult by mutableStateOf<JoinResult?>(null)

    fun createHousehold(name: String) =
        viewModelScope.launch {
            repository.createHousehold(name)
        }

    fun generateInvitationCode(householdId: Int) =
        viewModelScope.launch {
            invitationCode = repository.generateInvitationCode(householdId)
        }

    fun joinHouseholdByCode(code: String) =
        viewModelScope.launch {
            val result = repository.joinHouseholdByCode(code)
            joinHouseholdResult = if (result) JoinResult.Success else JoinResult.Failed
        }

    fun getHouseholdMembers(householdId: Int) = repository.getHouseholdMembers(householdId)

    fun removeMemberFromHousehold(member: HouseholdMember) =
        viewModelScope.launch {
            repository.removeMemberFromHousehold(member)
        }
}
