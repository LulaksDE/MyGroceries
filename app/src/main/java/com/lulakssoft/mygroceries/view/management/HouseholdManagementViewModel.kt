package com.lulakssoft.mygroceries.view.management

import android.content.Intent
import android.util.Log
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
    var selectedHouseholdId by mutableStateOf(0)
    var selectedHouseholdFirestoreId by mutableStateOf("")

    fun generateInvitationCode(
        firestoreId: String,
        onCodeGenerated: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (firestoreId.isNotEmpty()) {
                    val invitationCode = repository.generateInvitationCode(firestoreId)
                    onCodeGenerated(invitationCode)
                } else {
                    Log.d("HouseholdManagementViewModel", "FirestoreId is empty")
                    onCodeGenerated("")
                }
            } catch (e: Exception) {
                Log.e("HouseholdManagementViewModel", "Error generating invitation code: ${e.message}")
                onCodeGenerated("")
            }
        }
    }

    fun shareInvitationCode(code: String) {
        // Implementierung zum Teilen des Codes über die Android-Sharing-API
        // Dies kann über eine Intent-Integration erfolgen
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Einladung zum Haushalt")
                putExtra(Intent.EXTRA_TEXT, "Hier ist dein Einladungscode: $code")
            }

        // Diese Methode benötigt Zugriff auf einen Context, der über einen ContextProvider oder ähnliches bereitgestellt werden sollte
        // Um dieses vollständig zu implementieren, müsste man den Context an das ViewModel übergeben oder eine andere Architektur verwenden
        // ContextCompat.startActivity(context, Intent.createChooser(intent, "Einladungscode teilen"), null)
    }

    fun joinHouseholdByCode(code: String) =
        viewModelScope.launch {
            val result = repository.joinHouseholdByCode(code)
            joinHouseholdResult = if (result) JoinResult.Success else JoinResult.Failed
            Log.d("HouseholdManagementViewModel", "Join result: $joinHouseholdResult")
        }

    fun getHouseholdMembers(firestoreId: String) = repository.getHouseholdMembers(firestoreId)

    fun removeMemberFromHousehold(member: HouseholdMember) =
        viewModelScope.launch {
            repository.removeMemberFromHousehold(member)
        }
}
