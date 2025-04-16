package com.lulakssoft.mygroceries.database.household

import com.google.firebase.auth.FirebaseAuth
import com.lulakssoft.mygroceries.database.product.Household
import com.lulakssoft.mygroceries.database.product.HouseholdDao
import com.lulakssoft.mygroceries.database.product.HouseholdInvitationDao
import com.lulakssoft.mygroceries.database.product.HouseholdMemberDao
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID

class HouseholdRepository(
    private val householdDao: HouseholdDao,
    private val memberDao: HouseholdMemberDao,
    private val invitationDao: HouseholdInvitationDao,
    private val firestoreManager: FirestoreManager,
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Haushalt erstellen
    suspend fun createHousehold(name: String): Int {
        val userId = currentUser?.uid ?: return -1
        val userName = currentUser.displayName ?: "User"

        // Neuen Haushalt erstellen
        val household =
            Household(
                householdName = name,
                createdByUserId = userId,
                createdAt = LocalDateTime.now(),
                isPrivate = false,
            )

        // Haushalt in Datenbank speichern und ID zurückbekommen
        val householdId = householdDao.insertHouseholdAndGetId(household).toInt()

        // Ersteller als Mitglied mit Owner-Rolle hinzufügen
        val member =
            HouseholdMember(
                householdId = householdId,
                userId = userId,
                userName = userName,
                role = MemberRole.OWNER,
            )
        memberDao.insertMember(member)

        // Mit Firestore synchronisieren
        firestoreManager.syncHousehold(household.copy(id = householdId))

        return householdId
    }

    // Einladungscode generieren
    suspend fun generateInvitationCode(householdId: Int): String {
        val userId = currentUser?.uid ?: return ""

        // Eindeutigen Code generieren
        val invitationCode = UUID.randomUUID().toString().take(8)

        // Einladung erstellen
        val invitation =
            HouseholdInvitation(
                invitationCode = invitationCode,
                householdId = householdId,
                createdByUserId = userId,
            )
        invitationDao.createInvitation(invitation)

        return invitationCode
    }

    // Einem Haushalt beitreten
    suspend fun joinHouseholdByCode(invitationCode: String): Boolean {
        val invitation = invitationDao.getInvitationByCode(invitationCode) ?: return false
        val userId = currentUser?.uid ?: return false

        // Prüfen, ob der Benutzer bereits Mitglied ist
        val userHouseholds = memberDao.getHouseholdsForUser(userId).first()
        val isAlreadyMember = userHouseholds.any { it.householdId == invitation.householdId }

        if (isAlreadyMember) {
            // Benutzer ist bereits Mitglied
            return true
        }

        // Benutzer als neues Mitglied hinzufügen
        val newMember =
            HouseholdMember(
                householdId = invitation.householdId,
                userId = userId,
                userName = currentUser.displayName ?: "Gast",
                role = MemberRole.MEMBER,
            )
        memberDao.insertMember(newMember)
        return true
    }

    // Haushalte für den aktuellen Benutzer abrufen
    fun getUserHouseholds() =
        currentUser?.uid?.let {
            memberDao.getHouseholdsForUser(it)
        }

    fun getHouseholdMembers(householdId: Int) = memberDao.getMembersForHousehold(householdId)

    suspend fun removeMemberFromHousehold(member: HouseholdMember) = memberDao.removeMember(member)

    suspend fun updateMemberRole(
        memberId: Int,
        newRole: MemberRole,
    ) = memberDao.updateMemberRole(memberId, newRole)
}
