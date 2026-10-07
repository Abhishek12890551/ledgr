package app.ledgr.data.repository

import app.ledgr.engine.*
import app.ledgr.data.db.dao.EventDao
import app.ledgr.data.db.entities.EventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class EventRepository(private val dao: EventDao) {
    
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

    val events: Flow<List<Event>> = dao.getAllEvents().map { list ->
        list.map { json.decodeFromString<Event>(it.payloadJson) }
    }
    
    suspend fun getEffectiveEventsSync(): List<Event> {
        val all = dao.getAllEventsSync().map { json.decodeFromString<Event>(it.payloadJson) }
        return Events.effective(all)
    }

    suspend fun processTransaction(newEvents: List<Event>): Result<Unit, EngineError> {
        val existingEffective = getEffectiveEventsSync()
        val error = Validate.transaction(existingEffective, newEvents)
        if (error != null) return Result.Err(error)

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
        dao.insertAll(entities)
        return Result.Ok(Unit)
    }
}
