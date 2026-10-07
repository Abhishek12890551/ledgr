package app.ledgr.engine

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainExactly

class GoldenScenariosTest : StringSpec({
    val A = PersonId("Abhi")
    val Ra = PersonId("Rahul")
    val Am = PersonId("Aman")
    val Ro = PersonId("Rohit")
    val S = PersonId("Sameer")

    val accUpi = AccountId("UPI")
    val accCard = AccountId("Card")

    fun expense(id: String, paidBy: PersonId, amount: Long, split: SplitSpec, shares: Map<PersonId, Long>, paidMap: Map<PersonId, Long> = mapOf(paidBy to amount), accountId: AccountId? = null, scope: Scope = Scope.Group(GroupId("G")), refundOf: EventId? = null): Expense {
        return Expense(
            EventId(id), scope, "2026-10-07", 0L, 0L, paidBy, "INR", null, null,
            Money(amount), "Test", null, accountId,
            paidMap.mapValues { Money(it.value) },
            split,
            shares.mapValues { Money(it.value) },
            refundOf, null
        )
    }

    "G2 — Cycle collapses (simplification value)" {
        val e1 = expense("E1", Ra, 1000_00L, SplitSpec.Equal(listOf(A, Ra)), mapOf(A to 500_00L, Ra to 500_00L))
        val e2 = expense("E2", Am, 1400_00L, SplitSpec.Equal(listOf(Am, Ra)), mapOf(Am to 700_00L, Ra to 700_00L))
        val e3 = expense("E3", A, 600_00L, SplitSpec.Equal(listOf(A, Am)), mapOf(A to 300_00L, Am to 300_00L))

        val evs = listOf(e1, e2, e3)
        val original = Settle.originalFlow(evs)
        
        // originalFlow is sorted by amount desc, from asc, to asc
        original shouldContainExactly listOf(
            Payment(Ra, Am, Money(700_00L)),
            Payment(A, Ra, Money(500_00L)),
            Payment(Am, A, Money(300_00L))
        )

        val nets = Balances.forGroup(evs)
        nets[A] shouldBe Money(-200_00L)
        nets[Ra] shouldBe Money(-200_00L)
        nets[Am] shouldBe Money(400_00L)

        val simplified = Settle.simplified(nets)
        // A -> Am 200, Ra -> Am 200. Order: amount desc, debtor asc
        simplified shouldContainExactly listOf(
            Payment(A, Am, Money(200_00L)),
            Payment(Ra, Am, Money(200_00L))
        )
    }

    "G4 — Itemized bill with tax and discount" {
        val e = Expense(
            EventId("E1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, A, "INR", null, null,
            Money(1050_00L), "Test", null, null,
            mapOf(A to Money(1050_00L)),
            SplitSpec.Itemized(
                listOf(Item("Abhi", Money(300_00L), listOf(A)), Item("Rahul", Money(200_00L), listOf(Ra)), Item("Aman", Money(500_00L), listOf(Am))),
                listOf(Extra("Tax", Money(100_00L)), Extra("Discount", Money(-50_00L)))
            ),
            mapOf(A to Money(315_00L), Ra to Money(210_00L), Am to Money(525_00L)),
            null, null
        )
        val computedShares = Splits.computeShares(e.split, e.total, e.id, listOf(A, Ra, Am))
        computedShares shouldBe Result.Ok(mapOf(A to Money(315_00L), Ra to Money(210_00L), Am to Money(525_00L)))

        val nets = Balances.forGroup(listOf(e))
        nets[A] shouldBe Money(735_00L)
        nets[Ra] shouldBe Money(-210_00L)
        nets[Am] shouldBe Money(-525_00L)
    }

    "G5 — Two payers on one expense" {
        val e = expense("E1", A, 1200_00L, SplitSpec.Equal(listOf(A, Ra, Am)), mapOf(A to 400_00L, Ra to 400_00L, Am to 400_00L), paidMap = mapOf(A to 700_00L, Ra to 500_00L))
        val nets = Balances.forGroup(listOf(e))
        nets[A] shouldBe Money(300_00L)
        nets[Ra] shouldBe Money(100_00L)
        nets[Am] shouldBe Money(-400_00L)

        val simplified = Settle.simplified(nets)
        simplified shouldContainExactly listOf(
            Payment(Am, A, Money(300_00L)),
            Payment(Am, Ra, Money(100_00L))
        )
        Settle.originalFlow(listOf(e)) shouldContainExactly listOf(
            Payment(Am, A, Money(300_00L)),
            Payment(Am, Ra, Money(100_00L))
        )
    }

    "G6 — Edit after a partial settlement" {
        val e1 = expense("E1", A, 2000_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 500_00L, Ra to 500_00L, Am to 500_00L, Ro to 500_00L))
        val s1 = Settlement(EventId("S1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, Ra, "INR", null, null, Ra, A, Money(300_00L), PayMethod.CASH, null)
        
        val netsBefore = Balances.forGroup(listOf(e1, s1))
        netsBefore[A] shouldBe Money(1200_00L)
        netsBefore[Ra] shouldBe Money(-200_00L)
        netsBefore[Am] shouldBe Money(-500_00L)
        netsBefore[Ro] shouldBe Money(-500_00L)

        val voidE1 = VoidEvent(EventId("V1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, A, "INR", null, null, EventId("E1"))
        val e2 = expense("E2", A, 1800_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 450_00L, Ra to 450_00L, Am to 450_00L, Ro to 450_00L)).copy(supersedes = EventId("E1"))

        val netsAfter = Balances.forGroup(Events.effective(listOf(e1, s1, voidE1, e2)))
        netsAfter[A] shouldBe Money(1050_00L)
        netsAfter[Ra] shouldBe Money(-150_00L)
        netsAfter[Am] shouldBe Money(-450_00L)
        netsAfter[Ro] shouldBe Money(-450_00L)
    }

    "G7 — Void the settlement instead" {
        val e1 = expense("E1", A, 2000_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 500_00L, Ra to 500_00L, Am to 500_00L, Ro to 500_00L))
        val s1 = Settlement(EventId("S1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, Ra, "INR", null, null, Ra, A, Money(300_00L), PayMethod.CASH, null)
        val voidS1 = VoidEvent(EventId("V1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, A, "INR", null, null, EventId("S1"))
        
        val nets = Balances.forGroup(Events.effective(listOf(e1, s1, voidS1)))
        nets[A] shouldBe Money(1500_00L)
        nets[Ra] shouldBe Money(-500_00L)
        nets[Am] shouldBe Money(-500_00L)
        nets[Ro] shouldBe Money(-500_00L)
    }

    "G8 — Partial refund with a different split" {
        val e1 = expense("E1", A, 2000_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 500_00L, Ra to 500_00L, Am to 500_00L, Ro to 500_00L))
        val refund = expense("E2", A, -400_00L, SplitSpec.Exact(mapOf(Ra to Money(-200_00L), Am to Money(-200_00L))), mapOf(Ra to -200_00L, Am to -200_00L), mapOf(A to -400_00L), refundOf = EventId("E1"))

        val evs = listOf(e1, refund)
        val nets = Balances.forGroup(evs)
        nets[A] shouldBe Money(1100_00L)
        nets[Ra] shouldBe Money(-300_00L)
        nets[Am] shouldBe Money(-300_00L)
        nets[Ro] shouldBe Money(-500_00L)
    }

    "G9 — Overpaying a settlement" {
        val e1 = expense("E1", A, 2000_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 500_00L, Ra to 500_00L, Am to 500_00L, Ro to 500_00L))
        val s1 = Settlement(EventId("S1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, Ra, "INR", null, null, Ra, A, Money(700_00L), PayMethod.CASH, null)

        val nets = Balances.forGroup(listOf(e1, s1))
        nets[A] shouldBe Money(800_00L)
        nets[Ra] shouldBe Money(200_00L)
        nets[Am] shouldBe Money(-500_00L)
        nets[Ro] shouldBe Money(-500_00L)
    }

    "G10 — Personal bridge and settlement" {
        val e1 = expense("E1", A, 1000_00L, SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to 250_00L, Ra to 250_00L, Am to 250_00L, Ro to 250_00L), accountId = accUpi)
        val accs = listOf(Account(accUpi, AccountType.UPI_BANK, Money(0L)))
        val snap1 = Personal.snapshot(listOf(e1), A, setOf(GroupId("G")), accs)
        
        snap1.spendingByMonthAndCategory["2026-10"]?.get(null) shouldBe Money(250_00L)
        snap1.accountBalances[accUpi] shouldBe Money(-1000_00L)
        snap1.receivable shouldBe Money(750_00L)

        val s1 = Settlement(EventId("S1"), Scope.Group(GroupId("G")), "2026-10-07", 0L, 0L, Am, "INR", null, null, Am, A, Money(250_00L), PayMethod.UPI, accUpi)
        val snap2 = Personal.snapshot(listOf(e1, s1), A, setOf(GroupId("G")), accs)
        
        snap2.spendingByMonthAndCategory["2026-10"]?.get(null) shouldBe Money(250_00L)
        snap2.accountBalances[accUpi] shouldBe Money(-750_00L)
        snap2.receivable shouldBe Money(500_00L)
    }

    "G11 — Card purchase and bill payment" {
        val accs = listOf(Account(accCard, AccountType.CARD, Money(0L)), Account(accUpi, AccountType.UPI_BANK, Money(0L)))
        val e1 = expense("E1", A, 1000_00L, SplitSpec.Equal(listOf(A)), mapOf(A to 1000_00L), accountId = accCard, scope = Scope.Personal)
        
        val snap1 = Personal.snapshot(listOf(e1), A, setOf(), accs)
        snap1.spendingByMonthAndCategory["2026-10"]?.get(null) shouldBe Money(1000_00L)
        snap1.accountBalances[accCard] shouldBe Money(-1000_00L)

        val t1 = Transfer(EventId("T1"), Scope.Personal, "2026-10-07", 0L, 0L, A, "INR", null, null, accUpi, accCard, Money(1000_00L))
        val snap2 = Personal.snapshot(listOf(e1, t1), A, setOf(), accs)
        
        snap2.spendingByMonthAndCategory["2026-10"]?.get(null) shouldBe Money(1000_00L)
        snap2.accountBalances[accCard] shouldBe Money(0L)
        snap2.accountBalances[accUpi] shouldBe Money(-1000_00L)
    }

    "G12 — Keeper is not a participant" {
        val e1 = expense("E1", Ra, 1000_00L, SplitSpec.Equal(listOf(Ra, Am, Ro)), mapOf(Ra to 333_00L, Am to 333_00L, Ro to 334_00L))
        val snap = Personal.snapshot(listOf(e1), A, setOf(GroupId("G")), listOf())
        
        snap.spendingByMonthAndCategory.isEmpty() shouldBe true
        snap.receivable shouldBe Money(0L)
        snap.payable shouldBe Money(0L)

        val nets = Balances.forGroup(listOf(e1))
        nets[Ra] shouldBe Money(667_00L)
    }

    "G13 — Merged people" {
        val RaK = PersonId("Rahul K")
        val e1 = expense("E1", Am, 500_00L, SplitSpec.Equal(listOf(Am, Ra)), mapOf(Am to 250_00L, Ra to 250_00L))
        val e2 = expense("E2", Am, 500_00L, SplitSpec.Equal(listOf(Am, RaK)), mapOf(Am to 250_00L, RaK to 250_00L))
        
        val netsBefore = Balances.forGroup(listOf(e1, e2))
        netsBefore[Ra] shouldBe Money(-250_00L)
        netsBefore[RaK] shouldBe Money(-250_00L)
        netsBefore[Am] shouldBe Money(500_00L)

        val mergedInto = mapOf(RaK to Ra)
        val resolvedEvents = Events.resolvePeople(listOf(e1, e2), mergedInto)
        val netsAfter = Balances.forGroup(resolvedEvents)
        netsAfter[Ra] shouldBe Money(-500_00L)
        netsAfter[RaK] shouldBe null
        netsAfter[Am] shouldBe Money(500_00L)
    }

    "G14 — Safe to spend today" {
        val safe = Budget.safeToSpendToday(Money(9000_00L), Money(3500_00L), Money(500_00L), 26)
        safe shouldBe Money(192_30L)

        val safe2 = Budget.safeToSpendToday(Money(9000_00L), Money(8500_00L), Money(500_00L), 26)
        safe2 shouldBe Money(0L)

        val safe3 = Budget.safeToSpendToday(Money(9000_00L), Money(9000_00L), Money(500_00L), 26)
        safe3 shouldBe Money(0L)
    }
})
