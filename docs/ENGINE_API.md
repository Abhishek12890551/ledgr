# ENGINE_API — Kotlin contract for `:engine`

The shapes below are the public API of the pure Kotlin/JVM `:engine` module. `:data`, `:app` and tests depend on them, so keep names and semantics stable. Logic is defined in `ENGINE_SPEC.md`. Executable expectations are in `GOLDEN_SCENARIOS.md` and `tools/reference_engine.py`.

Package root (placeholder): `app.ledgr.engine`. No Android imports. No `Float`/`Double` anywhere in this module.

## 1. Primitive types

```kotlin
@JvmInline value class Money(val paise: Long) {
    operator fun plus(o: Money) = Money(paise + o.paise)
    operator fun minus(o: Money) = Money(paise - o.paise)
    operator fun unaryMinus() = Money(-paise)
    val isZero get() = paise == 0L
}
@JvmInline value class PersonId(val value: String)
@JvmInline value class GroupId(val value: String)
@JvmInline value class AccountId(val value: String)
@JvmInline value class CategoryId(val value: String)
@JvmInline value class EventId(val value: String)

sealed interface Scope {
    data object Personal : Scope
    data class Group(val id: GroupId) : Scope
}
```

## 2. Split specification

```kotlin
data class Item(val label: String, val amount: Money, val people: List<PersonId>)
data class Extra(val label: String, val amount: Money)   // signed: tax/tip/delivery > 0, discount < 0

sealed interface SplitSpec {
    data class Equal(val participants: List<PersonId>) : SplitSpec
    data class Exact(val amounts: Map<PersonId, Money>) : SplitSpec
    data class Percent(val basisPoints: Map<PersonId, Int>) : SplitSpec   // must sum to 10_000
    data class Shares(val shares: Map<PersonId, Int>) : SplitSpec
    data class Itemized(val items: List<Item>, val extras: List<Extra>) : SplitSpec
}
```

## 3. Events

```kotlin
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

data class Transfer(/* common fields */ val from: AccountId, val to: AccountId, val amount: Money) : Event
data class Lending(/* common fields */ val person: PersonId, val amount: Money, val accountId: AccountId) : Event
data class Borrowing(/* common fields */ val person: PersonId, val amount: Money, val accountId: AccountId) : Event
data class Settlement(/* common fields */ val from: PersonId, val to: PersonId, val amount: Money,
                      val method: PayMethod, val accountId: AccountId?) : Event
data class Void(/* common fields */ val targetId: EventId) : Event

enum class PayMethod { CASH, UPI, OTHER }
```

(`/* common fields */` stands for the full common property list from `Event`. Write them out in code.)

## 4. Core functions

```kotlin
object Splits {
    /** Largest-remainder allocation. Σ result == total exactly. Deterministic for a given tieKey. */
    fun allocate(total: Money, weights: List<Long>, tieKey: String): List<Money>

    /** Computes per-person shares for an expense. `order` = participants in group join order. */
    fun computeShares(spec: SplitSpec, total: Money, eventId: EventId, order: List<PersonId>): Result<Map<PersonId, Money>, EngineError>
}

object Events {
    /** Events not targeted by any Void. Voids themselves are excluded. */
    fun effective(all: List<Event>): List<Event>
    /** Follows mergedInto chains so merged people count as one. */
    fun resolvePeople(events: List<Event>, mergedInto: Map<PersonId, PersonId>): List<Event>
}

object Balances {
    /** net[p] = Σ(paid − shares) + settlements sent − settlements received. Σ == 0 or throws InvariantViolation. */
    fun forGroup(effectiveGroupEvents: List<Event>): Map<PersonId, Money>
}

data class Payment(val from: PersonId, val to: PersonId, val amount: Money)

object Settle {
    /** Greedy simplification: ≤ n−1 payments. Ties broken by amount desc, then PersonId asc. */
    fun simplified(nets: Map<PersonId, Money>): List<Payment>
    /** Original-flow view: pairwise netted debts per expense, with settlements applied to the pair. */
    fun originalFlow(effectiveGroupEvents: List<Event>): List<Payment>
}
```

## 5. Personal ledger and bridge

```kotlin
data class PersonalSnapshot(
    val spendingByMonthAndCategory: Map<String /*yyyy-MM*/, Map<CategoryId?, Money>>,
    val accountBalances: Map<AccountId, Money>,
    val receivable: Money,                     // money owed to me
    val payable: Money,                        // money I owe
    val personNets: Map<PersonId, Money>       // + they owe me, − I owe them (ledger + groups)
)

object Personal {
    fun snapshot(
        effectiveEvents: List<Event>,
        me: PersonId,
        groupsWhereIAmParticipant: Set<GroupId>,
        accounts: List<Account>
    ): PersonalSnapshot
}

data class Account(val id: AccountId, val type: AccountType, val openingBalance: Money)
enum class AccountType { CASH, UPI_BANK, CARD }

object Budget {
    /** floor((monthBudget − spent − upcomingRecurring) / daysLeftInclusiveToday), min 0. */
    fun safeToSpendToday(monthBudget: Money, spent: Money, upcomingRecurring: Money, daysLeftInclusive: Int): Money
}
```

## 6. Validation (called by `:data` before any write)

```kotlin
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
    fun transaction(existingEffective: List<Event>, newEvents: List<Event>): EngineError?
    /** Describes the effect of a proposed edit for the "This changes Rahul's balance by +₹120" message. */
    fun editImpact(existingEffective: List<Event>, voided: EventId, replacement: Event?): Map<PersonId, Money>
}
```

`Result<T, E>` is a small sealed class (`Ok`, `Err`) defined in `:engine`, not `kotlin.Result`, so the error type is typed.

## 7. Export model

```kotlin
@Serializable data class ExportFile(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val persons: List<PersonDto>, val groups: List<GroupDto>,
    val accounts: List<AccountDto>, val categories: List<CategoryDto>,
    val events: List<EventDto>            // includes Void events
)
object Merge { fun importInto(existingEventIds: Set<EventId>, file: ExportFile): List<EventDto> /* new ones only */ }
```

## 8. Rules for implementers
- Functions are pure and take explicit inputs. No clocks, randomness or I/O inside `:engine` (the caller passes IDs and timestamps).
- Every public function has KDoc stating its invariants.
- Property-based tests (Kotest) cover `allocate`, `Balances`, `Settle.simplified`, replay-order independence and export/import round trip.
- The Kotlin engine's outputs for every scenario in `GOLDEN_SCENARIOS.md` must match exactly.
- If an API change is needed, update this file and note it in `docs/PROGRESS.md`.
