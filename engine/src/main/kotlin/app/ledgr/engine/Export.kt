package app.ledgr.engine

import kotlinx.serialization.Serializable

@Serializable data class ExportFile(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val persons: List<PersonDto>, val groups: List<GroupDto>,
    val accounts: List<AccountDto>, val categories: List<CategoryDto>,
    val events: List<EventDto>            // includes Void events
)

@Serializable data class PersonDto(val id: String, val name: String, val status: String, val mergedInto: String?, val isMe: Boolean)
@Serializable data class GroupDto(val id: String, val name: String, val status: String, val memberIds: List<String>, val revision: Int, val createdAtMillis: Long)
@Serializable data class AccountDto(val id: String, val name: String, val type: String, val openingBalancePaise: Long)
@Serializable data class CategoryDto(val id: String, val name: String, val icon: String)
@Serializable data class EventDto(val id: String, val payloadJson: String)

object Merge { 
    fun importInto(existingEventIds: Set<EventId>, file: ExportFile): List<EventDto> /* new ones only */ {
        TODO("Not yet implemented")
    }
}
