package app.ledgr.engine

object Splits {
    /** Largest-remainder allocation. Σ result == total exactly. Deterministic for a given tieKey. */
    fun allocate(total: Money, weights: List<Long>, tieKey: String): List<Money> {
        val n = weights.size
        if (n == 0) return emptyList()
        val sumW = weights.sum()
        require(sumW > 0) { "Sum of weights must be > 0" }

        val isNegative = total.paise < 0
        val t = kotlin.math.abs(total.paise)

        val tBig = java.math.BigInteger.valueOf(t)
        val sumWBig = java.math.BigInteger.valueOf(sumW)

        val base = LongArray(n)
        val rem = LongArray(n)

        for (i in 0 until n) {
            val wBig = java.math.BigInteger.valueOf(weights[i])
            val prod = tBig * wBig
            base[i] = (prod / sumWBig).toLong()
            rem[i] = (prod % sumWBig).toLong()
        }

        val leftover = (t - base.sum()).toInt()
        
        val rotation = run {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = md.digest(tieKey.toByteArray())
            val intVal = ((hash[0].toInt() and 0xFF) shl 24) or
                         ((hash[1].toInt() and 0xFF) shl 16) or
                         ((hash[2].toInt() and 0xFF) shl 8) or
                         (hash[3].toInt() and 0xFF)
            (intVal.toLong() and 0xFFFFFFFFL) % n
        }.toInt()

        val order = (0 until n).sortedWith(Comparator { i, j ->
            val remCmp = rem[j].compareTo(rem[i]) // descending
            if (remCmp != 0) remCmp
            else {
                val posI = (i - rotation).let { if (it < 0) it + n else it } % n
                val posJ = (j - rotation).let { if (it < 0) it + n else it } % n
                posI.compareTo(posJ) // ascending
            }
        })

        for (i in 0 until leftover) {
            base[order[i]] += 1L
        }

        return base.map { if (isNegative) Money(-it) else Money(it) }
    }

    /** Computes per-person shares for an expense. `order` = participants in group join order. */
    fun computeShares(spec: SplitSpec, total: Money, eventId: EventId, order: List<PersonId>): Result<Map<PersonId, Money>, EngineError> {
        val totalPaise = total.paise
        return when (spec) {
            is SplitSpec.Equal -> {
                val pOrder = order.filter { spec.participants.contains(it) } + spec.participants.filter { !order.contains(it) }
                val weights = pOrder.map { 1L }
                val allocated = allocate(total, weights, eventId.value)
                Result.Ok(pOrder.zip(allocated).toMap())
            }
            is SplitSpec.Exact -> {
                val sum = spec.amounts.values.sumOf { it.paise }
                if (sum != totalPaise) Result.Err(EngineError.ExactDontSum(Money(totalPaise - sum)))
                else Result.Ok(spec.amounts)
            }
            is SplitSpec.Percent -> {
                val sum = spec.basisPoints.values.sum()
                if (sum != 10000) Result.Err(EngineError.PercentNot100(sum))
                else {
                    val pOrder = order.filter { spec.basisPoints.containsKey(it) } + spec.basisPoints.keys.filter { !order.contains(it) }
                    val weights = pOrder.map { spec.basisPoints[it]?.toLong() ?: 0L }
                    val allocated = allocate(total, weights, eventId.value)
                    Result.Ok(pOrder.zip(allocated).toMap())
                }
            }
            is SplitSpec.Shares -> {
                val pOrder = order.filter { spec.shares.containsKey(it) } + spec.shares.keys.filter { !order.contains(it) }
                val weights = pOrder.map { spec.shares[it]?.toLong() ?: 0L }
                val allocated = allocate(total, weights, eventId.value)
                Result.Ok(pOrder.zip(allocated).toMap())
            }
            is SplitSpec.Itemized -> {
                val subtotals = mutableMapOf<PersonId, Long>()
                var itemsSum = 0L
                spec.items.forEachIndexed { idx, item ->
                    itemsSum += item.amount.paise
                    val pOrder = order.filter { item.people.contains(it) } + item.people.filter { !order.contains(it) }
                    val weights = pOrder.map { 1L }
                    val allocated = allocate(item.amount, weights, "${eventId.value}:item:$idx")
                    pOrder.zip(allocated).forEach { (p, a) ->
                        subtotals[p] = (subtotals[p] ?: 0L) + a.paise
                    }
                }
                val extrasSum = spec.extras.sumOf { it.amount.paise }
                if (itemsSum + extrasSum != totalPaise) {
                    return Result.Err(EngineError.ItemizedTotalMismatch(total, Money(itemsSum + extrasSum)))
                }
                
                val pOrder = order.filter { subtotals.containsKey(it) } + subtotals.keys.filter { !order.contains(it) }
                val weights = pOrder.map { subtotals[it] ?: 0L }
                val extraShares = allocate(Money(extrasSum), weights, "${eventId.value}:extras")
                
                val result = mutableMapOf<PersonId, Money>()
                pOrder.zip(extraShares).forEach { (p, e) ->
                    val sub = subtotals[p] ?: 0L
                    result[p] = Money(sub + e.paise)
                }
                Result.Ok(result)
            }
        }
    }
}

object Events {
    /** Events not targeted by any Void. Voids themselves are excluded. */
    fun effective(all: List<Event>): List<Event> {
        val voidedTargets = all.filterIsInstance<Void>().map { it.targetId }.toSet()
        return all.filter { it !is Void && it.id !in voidedTargets }
    }
    
    /** Follows mergedInto chains so merged people count as one. */
    fun resolvePeople(events: List<Event>, mergedInto: Map<PersonId, PersonId>): List<Event> {
        fun resolve(p: PersonId): PersonId {
            var curr = p
            while (mergedInto.containsKey(curr)) {
                curr = mergedInto[curr]!!
            }
            return curr
        }
        
        return events.map { e ->
            when (e) {
                is Expense -> {
                    val resolvedPaid = mutableMapOf<PersonId, Money>()
                    e.paid.forEach { (p, m) ->
                        val rp = resolve(p)
                        resolvedPaid[rp] = Money((resolvedPaid[rp]?.paise ?: 0L) + m.paise)
                    }
                    val resolvedShares = mutableMapOf<PersonId, Money>()
                    e.shares.forEach { (p, m) ->
                        val rp = resolve(p)
                        resolvedShares[rp] = Money((resolvedShares[rp]?.paise ?: 0L) + m.paise)
                    }
                    e.copy(enteredBy = resolve(e.enteredBy), paid = resolvedPaid, shares = resolvedShares)
                }
                is Settlement -> e.copy(enteredBy = resolve(e.enteredBy), from = resolve(e.from), to = resolve(e.to))
                is Lending -> e.copy(enteredBy = resolve(e.enteredBy), person = resolve(e.person))
                is Borrowing -> e.copy(enteredBy = resolve(e.enteredBy), person = resolve(e.person))
                is Transfer -> e.copy(enteredBy = resolve(e.enteredBy))
                is Void -> e.copy(enteredBy = resolve(e.enteredBy))
            }
        }
    }
}

object Balances {
    /** net[p] = Σ(paid − shares) + settlements sent − settlements received. Σ == 0 or throws InvariantViolation. */
    fun forGroup(effectiveGroupEvents: List<Event>): Map<PersonId, Money> {
        val nets = mutableMapOf<PersonId, Long>()
        for (e in effectiveGroupEvents) {
            when (e) {
                is Expense -> {
                    e.paid.forEach { (p, m) -> nets[p] = (nets[p] ?: 0L) + m.paise }
                    e.shares.forEach { (p, m) -> nets[p] = (nets[p] ?: 0L) - m.paise }
                }
                is Settlement -> {
                    nets[e.from] = (nets[e.from] ?: 0L) + e.amount.paise
                    nets[e.to] = (nets[e.to] ?: 0L) - e.amount.paise
                }
                else -> {}
            }
        }
        val sum = nets.values.sum()
        if (sum != 0L) throw IllegalArgumentException("Group nets do not sum to zero: $sum paise")
        return nets.mapValues { Money(it.value) }
    }
}

data class Payment(val from: PersonId, val to: PersonId, val amount: Money)

object Settle {
    /** Greedy simplification: ≤ n−1 payments. Ties broken by amount desc, then PersonId asc. */
    fun simplified(nets: Map<PersonId, Money>): List<Payment> {
        val cred = nets.filter { it.value.paise > 0 }.mapValues { it.value.paise }.toMutableMap()
        val debt = nets.filter { it.value.paise < 0 }.mapValues { -it.value.paise }.toMutableMap()
        val out = mutableListOf<Payment>()
        while (cred.isNotEmpty() && debt.isNotEmpty()) {
            val c = cred.entries.sortedWith(compareBy({ -it.value }, { it.key.value })).first()
            val d = debt.entries.sortedWith(compareBy({ -it.value }, { it.key.value })).first()
            val x = minOf(c.value, d.value)
            out.add(Payment(d.key, c.key, Money(x)))
            cred[c.key] = c.value - x
            debt[d.key] = d.value - x
            if (cred[c.key] == 0L) cred.remove(c.key)
            if (debt[d.key] == 0L) debt.remove(d.key)
        }
        return out
    }
    
    /** Original-flow view: pairwise netted debts per expense, with settlements applied to the pair. */
    fun originalFlow(effectiveGroupEvents: List<Event>): List<Payment> {
        val pairDebts = mutableMapOf<Pair<PersonId, PersonId>, Long>()
        fun addDebt(from: PersonId, to: PersonId, amount: Long) {
            if (amount == 0L) return
            val existing = pairDebts[Pair(from, to)] ?: 0L
            pairDebts[Pair(from, to)] = existing + amount
        }
        
        for (e in effectiveGroupEvents) {
            when (e) {
                is Expense -> {
                    val nets = mutableMapOf<PersonId, Long>()
                    e.shares.forEach { (p, m) -> nets[p] = (nets[p] ?: 0L) - m.paise }
                    e.paid.forEach { (p, m) -> nets[p] = (nets[p] ?: 0L) + m.paise }
                    
                    val surplus = nets.filter { it.value > 0 }
                    val deficit = nets.filter { it.value < 0 }
                    
                    val sKeys = surplus.keys.toList()
                    val sWeights = sKeys.map { surplus[it]!! }
                    
                    for ((dPerson, dAmt) in deficit) {
                        val toAllocate = Money(-dAmt)
                        val allocated = Splits.allocate(toAllocate, sWeights, "${e.id.value}:flow")
                        sKeys.zip(allocated).forEach { (sPerson, amt) ->
                            addDebt(dPerson, sPerson, amt.paise)
                        }
                    }
                }
                is Settlement -> {
                    addDebt(e.to, e.from, e.amount.paise)
                }
                else -> {}
            }
        }
        
        val finalDebts = mutableListOf<Payment>()
        val seen = mutableSetOf<Pair<PersonId, PersonId>>()
        
        for ((pair, amtAB) in pairDebts) {
            val (a, b) = pair
            if (seen.contains(Pair(a, b))) continue
            
            val amtBA = pairDebts[Pair(b, a)] ?: 0L
            seen.add(Pair(a, b))
            seen.add(Pair(b, a))
            
            val net = amtAB - amtBA
            if (net > 0) {
                finalDebts.add(Payment(a, b, Money(net)))
            } else if (net < 0) {
                finalDebts.add(Payment(b, a, Money(-net)))
            }
        }
        
        return finalDebts.sortedWith(compareBy({ -it.amount.paise }, { it.from.value }, { it.to.value }))
    }
}
