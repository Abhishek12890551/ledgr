package app.ledgr.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.ledgr.data.db.entities.*
import app.ledgr.data.db.dao.*

@Database(
    entities = [
        EventEntity::class,
        PersonEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LedgrDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
}
