package com.lulakssoft.mygroceries.database.household

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM household_table ORDER BY householdName ASC")
    fun selectAllHouseholdsSortedByName(): Flow<List<Household>>

    @Insert
    suspend fun insertHousehold(household: Household)

    @Query("DELETE FROM household_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(household: Household)

    @Query("SELECT * FROM household_table WHERE id = :householdId")
    suspend fun getHouseholdById(householdId: Int): Household?

    @Query(
        "SELECT * FROM household_table, household_member_table " +
            "WHERE :userId = household_member_table.userId " +
            "AND household_table.id = household_member_table.householdId",
    )
    fun getHouseholdsByUserId(userId: String): Flow<List<Household>>

    @Insert
    suspend fun insertHouseholdAndGetId(household: Household): Long

    @Query("SELECT * FROM household_table WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getHouseholdByFirestoreId(firestoreId: String): Household?

    @Query("UPDATE household_table SET householdName = :name, isPrivate = :isPrivate WHERE id = :id")
    suspend fun updateHousehold(
        id: Int,
        name: String,
        isPrivate: Boolean,
    )

    @Query("SELECT * FROM household_activity_table WHERE firestoreId = :firestoreId ORDER BY timestamp DESC LIMIT 10")
    suspend fun getActivitiesForHousehold(firestoreId: String): List<HouseholdActivity>

    @Insert
    suspend fun insertActivity(activity: HouseholdActivity)

    @Query("SELECT * FROM household_activity_table WHERE activityId = :activityId LIMIT 1")
    suspend fun getActivityById(activityId: String): HouseholdActivity?

    @Update
    suspend fun updateActivity(activity: HouseholdActivity)
}

@Dao
interface HouseholdMemberDao {
    @Query("SELECT * FROM household_member_table WHERE firestoreId = :firestoreId")
    fun getMembersForHousehold(firestoreId: String): Flow<List<HouseholdMember>>

    @Query("SELECT * FROM household_member_table WHERE userId = :userId")
    fun getHouseholdsForUser(userId: String): Flow<List<HouseholdMember>>

    @Query("SELECT COUNT(*) FROM household_member_table WHERE firestoreId = :firestoreId")
    suspend fun getMemberCountForHousehold(firestoreId: String): Int

    @Insert
    suspend fun insertMember(member: HouseholdMember)

    @Delete
    suspend fun removeMember(member: HouseholdMember)

    @Query("UPDATE household_member_table SET role = :newRole WHERE id = :memberId")
    suspend fun updateMemberRole(
        memberId: Int,
        newRole: MemberRole,
    )

    @Query("UPDATE household_member_table SET role = :role, userName = :userName WHERE id = :id")
    suspend fun updateMember(
        id: Int,
        role: MemberRole,
        userName: String,
    )

    @Query("SELECT * FROM household_member_table WHERE firestoreId = :firestoreId AND userId = :userId LIMIT 1")
    suspend fun getMemberInHousehold(
        firestoreId: String,
        userId: String,
    ): HouseholdMember?
}

@Dao
interface HouseholdInvitationDao {
    @Query("SELECT * FROM household_invitation_table WHERE firestoreId = :firestoreId")
    fun getInvitationsForHousehold(firestoreId: String): Flow<List<HouseholdInvitation>>

    @Query("SELECT * FROM household_invitation_table WHERE invitationCode = :code")
    suspend fun getInvitationByCode(code: String): HouseholdInvitation?

    @Insert
    suspend fun createInvitation(invitation: HouseholdInvitation)

    @Delete
    suspend fun deleteInvitation(invitation: HouseholdInvitation)
}
