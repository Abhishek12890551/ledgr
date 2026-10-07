package app.ledgr.engine

sealed interface EngineError {
    data class SharesDontSum(val expected: Money, val actual: Money) : EngineError
    data class PaidDontSum(val expected: Money, val actual: Money) : EngineError
    data class PercentNot100(val actualBasisPoints: Int) : EngineError
    data class ExactDontSum(val remaining: Money) : EngineError     // remaining may be negative
    data class ItemizedTotalMismatch(val expected: Money, val actual: Money) : EngineError
    data class UnknownParticipant(val id: PersonId) : EngineError
    data class NonPositiveAmount(val field: String) : EngineError
    data class InvariantViolation(val message: String) : EngineError
}

object Validate {
    /** Returns null if the transaction (one or more events) can be written; otherwise the first error. */
    fun transaction(existingEffective: List<Event>, newEvents: List<Event>): EngineError? {
        TODO("Not yet implemented")
    }

    /** Describes the effect of a proposed edit for the "This changes Rahul's balance by +₹120" message. */
    fun editImpact(existingEffective: List<Event>, voided: EventId, replacement: Event?): Map<PersonId, Money> {
        TODO("Not yet implemented")
    }
}
