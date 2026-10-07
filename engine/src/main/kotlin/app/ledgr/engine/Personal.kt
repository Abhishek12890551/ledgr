package app.ledgr.engine

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
    ): PersonalSnapshot {
        val spending = mutableMapOf<String, MutableMap<CategoryId?, Long>>()
        val accBals = accounts.associate { it.id to it.openingBalance.paise }.toMutableMap()
        val personNets = mutableMapOf<PersonId, Long>()

        fun addSpending(month: String, cat: CategoryId?, amt: Long) {
            if (amt == 0L) return
            val m = spending.getOrPut(month) { mutableMapOf() }
            m[cat] = (m[cat] ?: 0L) + amt
        }

        for (e in effectiveEvents) {
            val scope = e.scope
            val isPersonal = scope == Scope.Personal
            val isGroupWithMe = scope is Scope.Group && groupsWhereIAmParticipant.contains(scope.id)
            val month = e.occurredDate.substring(0, 7)
            
            when (e) {
                is Expense -> {
                    if (isPersonal) addSpending(month, e.categoryId, e.total.paise)
                    else if (isGroupWithMe) addSpending(month, e.categoryId, e.shares[me]?.paise ?: 0L)

                    val myPaid = e.paid[me]?.paise ?: 0L
                    if (myPaid != 0L && e.accountId != null) {
                        accBals[e.accountId] = (accBals[e.accountId] ?: 0L) - myPaid
                    }
                }
                is Transfer -> {
                    accBals[e.from] = (accBals[e.from] ?: 0L) - e.amount.paise
                    accBals[e.to] = (accBals[e.to] ?: 0L) + e.amount.paise
                }
                is Lending -> {
                    if (e.enteredBy == me) { 
                        accBals[e.accountId] = (accBals[e.accountId] ?: 0L) - e.amount.paise
                        personNets[e.person] = (personNets[e.person] ?: 0L) + e.amount.paise
                    }
                }
                is Borrowing -> {
                    if (e.enteredBy == me) {
                        accBals[e.accountId] = (accBals[e.accountId] ?: 0L) + e.amount.paise
                        personNets[e.person] = (personNets[e.person] ?: 0L) - e.amount.paise
                    }
                }
                is Settlement -> {
                    if (isPersonal) {
                        if (e.from == me) {
                            if (e.accountId != null) accBals[e.accountId] = (accBals[e.accountId] ?: 0L) - e.amount.paise
                            personNets[e.to] = (personNets[e.to] ?: 0L) + e.amount.paise
                        } else if (e.to == me) {
                            if (e.accountId != null) accBals[e.accountId] = (accBals[e.accountId] ?: 0L) + e.amount.paise
                            personNets[e.from] = (personNets[e.from] ?: 0L) - e.amount.paise
                        }
                    } else if (isGroupWithMe) {
                        if (e.from == me) {
                            if (e.accountId != null) accBals[e.accountId] = (accBals[e.accountId] ?: 0L) - e.amount.paise
                        } else if (e.to == me) {
                            if (e.accountId != null) accBals[e.accountId] = (accBals[e.accountId] ?: 0L) + e.amount.paise
                        }
                    }
                }
                else -> {}
            }
        }
        
        var rec = 0L
        var pay = 0L
        
        val personalNetsOnly = personNets.toMap()
        for ((_, net) in personalNetsOnly) {
            if (net > 0) rec += net
            else if (net < 0) pay += -net
        }
        
        val groupEvents = effectiveEvents.filter { it.scope is Scope.Group }.groupBy { it.scope as Scope.Group }
        for ((groupScope, evs) in groupEvents) {
            if (groupsWhereIAmParticipant.contains(groupScope.id)) {
                val groupBals = Balances.forGroup(evs)
                val myNet = groupBals[me]?.paise ?: 0L
                if (myNet > 0) rec += myNet
                else if (myNet < 0) pay += -myNet
                
                val flows = Settle.originalFlow(evs)
                for (f in flows) {
                    if (f.from == me) personNets[f.to] = (personNets[f.to] ?: 0L) - f.amount.paise
                    else if (f.to == me) personNets[f.from] = (personNets[f.from] ?: 0L) + f.amount.paise
                }
            }
        }
        
        return PersonalSnapshot(
            spendingByMonthAndCategory = spending.mapValues { it.value.mapValues { Money(it.value) } },
            accountBalances = accBals.mapValues { Money(it.value) },
            receivable = Money(rec),
            payable = Money(pay),
            personNets = personNets.mapValues { Money(it.value) }
        )
    }
}

data class Account(val id: AccountId, val type: AccountType, val openingBalance: Money)
enum class AccountType { CASH, UPI_BANK, CARD }

object Budget {
    /** floor((monthBudget − spent − upcomingRecurring) / daysLeftInclusiveToday), min 0. */
    fun safeToSpendToday(monthBudget: Money, spent: Money, upcomingRecurring: Money, daysLeftInclusive: Int): Money {
        if (daysLeftInclusive <= 0) return Money(0L)
        val safe = (monthBudget.paise - spent.paise - upcomingRecurring.paise) / daysLeftInclusive
        return if (safe > 0) Money(safe) else Money(0L)
    }
}
