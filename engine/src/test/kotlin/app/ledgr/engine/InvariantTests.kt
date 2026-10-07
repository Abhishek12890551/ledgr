package app.ledgr.engine

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

val personIdArb = arbitrary { PersonId(Arb.stringPattern("[a-z]{5}").bind()) }
val moneyArb = arbitrary { Money(Arb.long(1L..10000000L).bind()) }

val expenseArb = arbitrary { rs ->
    val id = EventId(Arb.stringPattern("[a-z]{10}").bind())
    val scope = Scope.Group(GroupId("G"))
    val people = List(5) { personIdArb.bind() }.distinct()
    val total = moneyArb.bind()
    val paid = mapOf(people.first() to total)
    val shares = Splits.allocate(total, people.map { 1L }, id.value)
    val sharesMap = people.zip(shares).toMap()
    Expense(
        id = id,
        scope = scope,
        occurredDate = "2026-10-07",
        occurredAtMillis = 0L,
        recordedAtMillis = 0L,
        enteredBy = people.first(),
        currency = "INR",
        supersedes = null,
        accountId = null,
        total = total,
        description = "Test",
        categoryId = null,
        note = null,
        paid = paid,
        split = SplitSpec.Equal(people),
        shares = sharesMap,
        refundOf = null,
        emoji = null
    )
}

class InvariantTests : StringSpec({
    "Nets sum to exactly zero for any set of group events" {
        checkAll(50, Arb.list(expenseArb, 2..20)) { generatedEvents ->
            val events = generatedEvents.distinctBy { it.id }
            val nets = Balances.forGroup(events)
            nets.values.sumOf { it.paise } shouldBe 0L
        }
    }

    "Order of events does not affect Balances or PersonalSnapshot" {
        checkAll(10, Arb.list(expenseArb, 2..10)) { generatedEvents ->
            val events = generatedEvents.distinctBy { it.id }
            if (events.isEmpty()) return@checkAll
            val nets1 = Balances.forGroup(events)
            val nets2 = Balances.forGroup(events.shuffled())
            nets1 shouldBe nets2

            val me = events.first().enteredBy
            val accounts = listOf<Account>()
            val snap1 = Personal.snapshot(events, me, setOf(GroupId("G")), accounts)
            val snap2 = Personal.snapshot(events.shuffled(), me, setOf(GroupId("G")), accounts)
            snap1 shouldBe snap2
        }
    }

    "VOID restores exact nets as before VOID" {
        checkAll(10, Arb.list(expenseArb, 2..10)) { generatedEvents ->
            val events = generatedEvents.distinctBy { it.id }
            if (events.isEmpty()) return@checkAll
            val target = events.last()
            val beforeVoidNets = Balances.forGroup(events)

            val voidEvent = VoidEvent(
                id = EventId("void1"),
                scope = target.scope,
                occurredDate = "2026-10-07",
                occurredAtMillis = 0L,
                recordedAtMillis = 0L,
                enteredBy = target.enteredBy,
                currency = "INR",
                supersedes = null,
                note = null,
                targetId = target.id
            )
            
            val targetCopy = (target as Expense).copy(id = EventId("targetCopy"), supersedes = target.id)
            
            val allEvents = events + voidEvent + targetCopy
            val effective = Events.effective(allEvents)
            
            val afterNets = Balances.forGroup(effective)
            
            afterNets shouldBe beforeVoidNets
        }
    }
})
