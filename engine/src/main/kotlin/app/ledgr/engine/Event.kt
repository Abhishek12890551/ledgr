package app.ledgr.engine

sealed interface Event {
    val id: EventId
    val scope: Scope
    val occurredDate: String      // local "yyyy-MM-dd", used for month bucketing
    val occurredAtMillis: Long
    val recordedAtMillis: Long    // set by the system, never edited
    val enteredBy: PersonId
    val currency: String          // "INR" in v1
    val supersedes: EventId?
    val note: String?
}

data class Expense(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val total: Money,                       // negative only for refunds
    val description: String,
    val categoryId: CategoryId?,
    val accountId: AccountId?,              // account "me" paid from, if "me" paid
    val paid: Map<PersonId, Money>,
    val split: SplitSpec,
    val shares: Map<PersonId, Money>,       // stored snapshot; replay never recomputes
    val refundOf: EventId?,
    val emoji: String?
) : Event

data class Transfer(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val from: AccountId, val to: AccountId, val amount: Money
) : Event

data class Lending(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val person: PersonId, val amount: Money, val accountId: AccountId
) : Event

data class Borrowing(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val person: PersonId, val amount: Money, val accountId: AccountId
) : Event

data class Settlement(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val from: PersonId, val to: PersonId, val amount: Money,
    val method: PayMethod, val accountId: AccountId?
) : Event

data class Void(
    override val id: EventId, override val scope: Scope, override val occurredDate: String,
    override val occurredAtMillis: Long, override val recordedAtMillis: Long,
    override val enteredBy: PersonId, override val currency: String,
    override val supersedes: EventId?, override val note: String?,
    val targetId: EventId
) : Event

enum class PayMethod { CASH, UPI, OTHER }
