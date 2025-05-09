package com.lulakssoft.mygroceries.database.household

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID

class HouseholdRepository(
    val householdDao: HouseholdDao,
    val memberDao: HouseholdMemberDao,
    private val invitationDao: HouseholdInvitationDao,
) {
    private val firestoreManager = FirestoreManager()
    val currentUser = FirebaseAuth.getInstance().currentUser

    suspend fun insertHouseholdAndGetId(household: Household): Long = householdDao.insertHouseholdAndGetId(household)

    fun getHouseholdsByUserId(Id: String): Flow<List<Household>> = householdDao.getHouseholdsByUserId(Id)

    suspend fun getMemberCountForHousehold(firestoreId: String): Int = memberDao.getMemberCountForHousehold(firestoreId)

    suspend fun deleteHousehold(household: Household) {
        householdDao.delete(household)
    }

    // Haushalt erstellen
    suspend fun createHousehold(name: String): Int {
        val userId = currentUser?.uid ?: return -1
        val userName = currentUser.displayName ?: "User"
        val firestoreId = UUID.randomUUID().toString()

        // Neuen Haushalt erstellen
        val household =
            Household(
                householdName = name,
                createdByUserId = userId,
                createdAt = LocalDateTime.now(),
                isPrivate = false,
                firestoreId = firestoreId,
            )

        // Haushalt in Datenbank speichern und ID zurückbekommen
        val householdId = householdDao.insertHouseholdAndGetId(household).toInt()

        // Ersteller als Mitglied mit Owner-Rolle hinzufügen
        val member =
            HouseholdMember(
                householdId = householdId,
                firestoreId = firestoreId,
                userId = userId,
                userName = userName,
                role = MemberRole.OWNER,
            )
        memberDao.insertMember(member)

        // Mit Firestore synchronisieren wenn Firestore ID nicht null ist
        if (household.firestoreId != null) {
            firestoreManager.syncNewHousehold(household, member.userName)
        } else {
            Log.e("HouseholdRepository", "Firestore ID is null")
        }
        return householdId
    }

    suspend fun generateInvitationCode(firestoreId: String): String {
        val userId = currentUser?.uid ?: return ""

        // Eindeutigen Code generieren
        val invitationCode = UUID.randomUUID().toString().take(8)

        // Einladung erstellen
        val invitation =
            HouseholdInvitation(
                invitationCode = invitationCode,
                firestoreId = firestoreId,
                createdByUserId = userId,
            )
        invitationDao.createInvitation(invitation)

        // Mit Firestore synchronisieren
        firestoreManager.syncInvitation(invitation)

        return invitationCode
    }

    suspend fun insertOrUpdateHousehold(household: Household): Long {
        val existingHousehold =
            household.firestoreId?.let { firestoreId ->
                // Suche nach Haushalt mit dieser firestoreId
                householdDao.getHouseholdByFirestoreId(firestoreId)
            }

        return if (existingHousehold == null) {
            // Einfügen als neuen Haushalt
            householdDao.insertHouseholdAndGetId(household)
        } else {
            // Aktualisieren des bestehenden Haushalts
            householdDao.updateHousehold(
                existingHousehold.id,
                household.householdName,
                household.isPrivate,
            )
            existingHousehold.id.toLong()
        }
    }

    suspend fun insertOrUpdateMember(member: HouseholdMember) {
        val existingMember =
            memberDao.getMemberInHousehold(
                member.firestoreId,
                member.userId,
            )

        if (existingMember == null) {
            memberDao.insertMember(member)
        } else {
            memberDao.updateMember(
                existingMember.id,
                member.role,
                member.userName,
            )
        }
    }

    suspend fun joinHouseholdByCode(invitationCode: String): Boolean {
        try {
            val firestoreInvitation = firestoreManager.getInvitationByCode(invitationCode) ?: return false

            // Prüfen ob isActive true ist
            if (firestoreInvitation["isActive"] != true) {
                Log.d("HouseholdRepository", "Invitation is not active")
                return false
            } else {
                val firestoreId = firestoreInvitation["firestoreId"] as? String ?: return false
                Log.d("HouseholdRepository", "Firestore ID that will be joined: $firestoreId")
                val userId = currentUser?.uid ?: return false
                val userName = currentUser.displayName ?: "User"
                Log.d("HouseholdRepository", "User ID that will join: $userId")

                // Prüfen, ob der Benutzer bereits Mitglied ist
                val userHouseholds = memberDao.getHouseholdsForUser(userId).first()
                Log.d("HouseholdRepository", "User households: $userHouseholds")
                val isAlreadyMember = userHouseholds.any { it.firestoreId == firestoreId }
                Log.d("HouseholdRepository", "Is already member: $isAlreadyMember")

                if (isAlreadyMember) {
                    Log.d("HouseholdRepository", "User is already a member of the household")
                    return true
                }
                // Haushalt beitreten
                firestoreManager.syncNewMember(firestoreId, userId, userName, "MEMBER")
                val joinedHousehold = firestoreManager.getHouseholdById(firestoreId)

                val household =
                    Household(
                        householdName = joinedHousehold?.get("householdName") as? String ?: "",
                        createdByUserId = joinedHousehold?.get("createdByUserId") as? String ?: "",
                        createdAt = LocalDateTime.parse(joinedHousehold?.get("createdAt") as? String),
                        isPrivate = joinedHousehold?.get("isPrivate") as? Boolean ?: false,
                        firestoreId = firestoreId,
                    )
                val householdId = householdDao.insertHouseholdAndGetId(household).toInt()
                Log.d("HouseholdRepository", "Created household: $household")

                // Mitglied hinzufügen
                val member =
                    HouseholdMember(
                        householdId = householdId,
                        firestoreId = firestoreId,
                        userId = userId,
                        userName = userName,
                        role = MemberRole.MEMBER,
                    )
                memberDao.insertMember(member)
                Log.d("HouseholdRepository", "Member added to household: $member")

                // Einladung als verwendet markieren (optional)
                firestoreManager.deactivateInvitation(invitationCode)

                return true
            }
        } catch (e: Exception) {
            Log.e("HouseholdRepository", "Error joining household: ${e.message}")
            return false
        }
    }

    suspend fun insertOrUpdateActivity(activity: HouseholdActivity) {
        val existingActivity = householdDao.getActivityById(activity.activityId)
        if (existingActivity == null) {
            householdDao.insertActivity(activity)
        } else {
            householdDao.updateActivity(activity)
        }
    }

    suspend fun getActivitiesForHousehold(firestoreId: String): List<HouseholdActivity> =
        householdDao.getActivitiesForHousehold(firestoreId)

    suspend fun getUserMembershipInHousehold(
        firestoreId: String,
        userId: String,
    ): HouseholdMember? = memberDao.getMemberInHousehold(firestoreId, userId)

    // Haushalte für den aktuellen Benutzer abrufen
    fun getUserHouseholds() =
        currentUser?.uid?.let {
            memberDao.getHouseholdsForUser(it)
        }

    fun getHouseholdMembers(firestoreId: String) = memberDao.getMembersForHousehold(firestoreId)

    suspend fun removeMemberFromHousehold(member: HouseholdMember) = memberDao.removeMember(member)

    suspend fun updateMemberRole(
        memberId: Int,
        newRole: MemberRole,
    ) = memberDao.updateMemberRole(memberId, newRole)
}
