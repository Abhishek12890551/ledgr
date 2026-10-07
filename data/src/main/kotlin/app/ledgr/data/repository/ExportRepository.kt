package app.ledgr.data.repository

import app.ledgr.engine.*
import app.ledgr.data.db.LedgrDatabase
import app.ledgr.data.db.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import androidx.room.withTransaction

class ExportRepository(
    private val db: LedgrDatabase,
    private val eventRepository: EventRepository
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(Event::class) {
                subclass(Expense::class)
                subclass(Transfer::class)
                subclass(Lending::class)
                subclass(Borrowing::class)
                subclass(Settlement::class)
                subclass(VoidEvent::class)
            }
        }
    }

    suspend fun export(): ExportFile {
        return db.withTransaction {
            val persons = db.personDao().getAllPersons().first().map { 
                PersonDto(it.id, it.name, it.status, it.mergedInto, it.isMe) 
            }
            val groups = db.groupDao().getAllGroups().first().map { g ->
                val members = db.groupMemberDao().getMembersForGroup(g.id).first().map { it.personId }
                GroupDto(g.id, g.name, g.status, members, g.revision, g.createdAt)
            }
            val accounts = db.accountDao().getAllAccounts().first().map {
                AccountDto(it.id, it.name, it.type, it.openingBalance)
            }
            val categories = db.categoryDao().getAllCategories().first().map {
                CategoryDto(it.id, it.name, it.icon)
            }
            val events = db.eventDao().getAllEventsSync().map {
                EventDto(it.id, it.payloadJson)
            }

            ExportFile(
                schemaVersion = 1,
                exportedAtMillis = System.currentTimeMillis(),
                persons = persons,
                groups = groups,
                accounts = accounts,
                categories = categories,
                events = events
            )
        }
    }

    suspend fun import(file: ExportFile) {
        db.withTransaction {
            db.personDao().insertAll(file.persons.map { PersonEntity(it.id, it.name, it.status, it.mergedInto, it.isMe) })
            
            db.groupDao().insertAll(file.groups.map { GroupEntity(it.id, it.name, it.status, it.revision, it.createdAtMillis) })
            
            val members = file.groups.flatMap { g -> g.memberIds.map { GroupMemberEntity(g.id, it) } }
            db.groupMemberDao().insertAll(members)
            
            db.accountDao().insertAll(file.accounts.map { AccountEntity(it.id, it.name, it.type, it.openingBalancePaise) })
            
            db.categoryDao().insertAll(file.categories.map { CategoryEntity(it.id, it.name, it.icon, 0) })
            
            val existingIds = db.eventDao().getAllEventsSync().map { it.id }.toSet()
            val newEventDtos = Merge.importInto(existingIds, file)
            
            val newEvents = newEventDtos.map { json.decodeFromString<Event>(it.payloadJson) }
            val entities = newEvents.map { e ->
                val scopeStr = when (e.scope) {
                    is Scope.Personal -> "Personal"
                    is Scope.Group -> "Group:${(e.scope as Scope.Group).id.value}"
                }
                EventEntity(
                    id = e.id.value,
                    scope = scopeStr,
                    type = e::class.simpleName ?: "Unknown",
                    occurredDate = e.occurredDate,
                    occurredAt = e.occurredAtMillis,
                    recordedAt = e.recordedAtMillis,
                    supersedes = e.supersedes?.value,
                    payloadJson = json.encodeToString(e)
                )
            }
            db.eventDao().insertAll(entities)
        }
    }
}
