package app.ledgr.engine

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainExactly

class GoldenTests : StringSpec({
    val A = PersonId("Abhi")
    val Ra = PersonId("Rahul")
    val Am = PersonId("Aman")
    val Ro = PersonId("Rohit")
    val S = PersonId("Sameer")
    val five = listOf(A, Ra, Am, Ro, S)

    "G1 — Goa trip" {
        val e1 = Expense(EventId("E1"), Scope.Group(GroupId("Goa")), "2026-10-07", 0L, 0L, A, "INR", null, null,
            Money(6000_00L), "Hotel", null, null, mapOf(A to Money(6000_00L)),
            SplitSpec.Equal(five), mapOf(A to Money(1200_00L), Ra to Money(1200_00L), Am to Money(1200_00L), Ro to Money(1200_00L), S to Money(1200_00L)), null, null)
        val e2 = Expense(EventId("E2"), Scope.Group(GroupId("Goa")), "2026-10-07", 0L, 0L, Ra, "INR", null, null,
            Money(2400_00L), "Food", null, null, mapOf(Ra to Money(2400_00L)),
            SplitSpec.Equal(five), mapOf(A to Money(480_00L), Ra to Money(480_00L), Am to Money(480_00L), Ro to Money(480_00L), S to Money(480_00L)), null, null)
        val e3 = Expense(EventId("E3"), Scope.Group(GroupId("Goa")), "2026-10-07", 0L, 0L, A, "INR", null, null,
            Money(3000_00L), "Fuel", null, null, mapOf(A to Money(3000_00L)),
            SplitSpec.Equal(listOf(A, Ra, Am, Ro)), mapOf(A to Money(750_00L), Ra to Money(750_00L), Am to Money(750_00L), Ro to Money(750_00L)), null, null)
        val e4 = Expense(EventId("E4"), Scope.Group(GroupId("Goa")), "2026-10-07", 0L, 0L, Am, "INR", null, null,
            Money(1500_00L), "Tickets", null, null, mapOf(Am to Money(1500_00L)),
            SplitSpec.Equal(five), mapOf(A to Money(300_00L), Ra to Money(300_00L), Am to Money(300_00L), Ro to Money(300_00L), S to Money(300_00L)), null, null)
        val e5 = Expense(EventId("E5"), Scope.Group(GroupId("Goa")), "2026-10-07", 0L, 0L, Ro, "INR", null, null,
            Money(800_00L), "Gifts", null, null, mapOf(Ro to Money(800_00L)),
            SplitSpec.Exact(mapOf(A to Money(200_00L), Ro to Money(300_00L), S to Money(300_00L))), mapOf(A to Money(200_00L), Ro to Money(300_00L), S to Money(300_00L)), null, null)
        
        val nets = Balances.forGroup(listOf(e1, e2, e3, e4, e5))
        nets[A] shouldBe Money(6070_00L)
        nets[Ra] shouldBe Money(-330_00L)
        nets[Am] shouldBe Money(-1230_00L)
        nets[Ro] shouldBe Money(-2230_00L)
        nets[S] shouldBe Money(-2280_00L)

        val payments = Settle.simplified(nets)
        payments shouldContainExactly listOf(
            Payment(S, A, Money(2280_00L)),
            Payment(Ro, A, Money(2230_00L)),
            Payment(Am, A, Money(1230_00L)),
            Payment(Ra, A, Money(330_00L))
        )
    }

    "G3 — Rounding is exact and deterministic" {
        Splits.allocate(Money(100_00L), listOf(1L, 1L, 1L), "g3-a").map { it.paise } shouldContainExactly listOf(3333L, 3333L, 3334L)
        Splits.allocate(Money(100_00L), listOf(1L, 1L, 1L), "g3-b").map { it.paise } shouldContainExactly listOf(3333L, 3334L, 3333L)
        Splits.allocate(Money(100_00L), listOf(1L, 1L, 1L), "g3-c").map { it.paise } shouldContainExactly listOf(3333L, 3334L, 3333L)
        Splits.allocate(Money(100_00L), listOf(1L, 1L, 1L), "g3-d").map { it.paise } shouldContainExactly listOf(3333L, 3333L, 3334L)
        
        Splits.allocate(Money(10_00L), listOf(1L, 1L, 1L), "g3-a").map { it.paise } shouldContainExactly listOf(333L, 333L, 334L)
        Splits.allocate(Money(10_01L), listOf(3L, 2L, 5L), "g4-r").map { it.paise } shouldContainExactly listOf(300L, 200L, 501L)
    }

})
