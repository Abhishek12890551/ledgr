package app.ledgr.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["scope", "occurred_date"]),
        Index(value = ["type"]),
        Index(value = ["supersedes"])
    ]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val scope: String,
    val type: String,
    @ColumnInfo(name = "occurred_date") val occurredDate: String,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    val supersedes: String?,
    @ColumnInfo(name = "payload_json") val payloadJson: String
)

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String,
    @ColumnInfo(name = "merged_into") val mergedInto: String?,
    @ColumnInfo(name = "is_me") val isMe: Boolean
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String,
    val revision: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["group_id", "person_id"]
)
data class GroupMemberEntity(
    @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "person_id") val personId: String
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    @ColumnInfo(name = "opening_balance") val openingBalance: Long
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val sort: Int
)

@Entity(
    tableName = "budgets",
    primaryKeys = ["month", "category_id"]
)
data class BudgetEntity(
    val month: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val amount: Long
)

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "template_payload_json") val templatePayloadJson: String,
    val rule: String,
    @ColumnInfo(name = "next_due") val nextDue: Long
)
