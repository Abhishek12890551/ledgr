package app.ledgr.engine

@JvmInline value class Money(val paise: Long) : Comparable<Money> {
    operator fun plus(o: Money) = Money(paise + o.paise)
    operator fun minus(o: Money) = Money(paise - o.paise)
    operator fun unaryMinus() = Money(-paise)
    val isZero get() = paise == 0L
    override fun compareTo(other: Money): Int = paise.compareTo(other.paise)
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
