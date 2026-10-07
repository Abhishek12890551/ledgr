package app.ledgr.data.db.dao

import androidx.room.*
import app.ledgr.data.db.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    suspend fun getAllEventsSync(): List<EventEntity>

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Insert
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: String): EventEntity?
}

@Dao
@JvmSuppressWildcards
interface PersonDao {
    @Query("SELECT * FROM persons")
    fun getAllPersons(): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: PersonEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(persons: List<PersonEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface GroupDao {
    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<GroupEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    fun getMembersForGroup(groupId: String): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(member: GroupMemberEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(members: List<GroupMemberEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>): List<Long>
}

@Dao
@JvmSuppressWildcards
interface RecurringDao {
    @Query("SELECT * FROM recurring")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recurring: List<RecurringEntity>): List<Long>
}
