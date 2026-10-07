# ENGINE_SPEC — money logic

This is the contract for the `:engine` module: a **pure Kotlin/JVM library with no Android dependencies**. Everything here must be covered by unit and property-based tests before any UI depends on it.

## 1. Money

- All amounts are `Long` **integer paise**. Never use Float/Double for money.
- v1 currency is `INR`. Every event still stores `currency` (and `original_amount` if later converted) so adding currencies needs no migration.
- Display: Indian digit grouping (₹1,23,456.50). Parsing user input converts to paise once at the edge.

## 2. Splitting with deterministic rounding

`allocate(total: Long, weights: List<Long>, tieKey: String): List<Long>` (largest-remainder method)

1. `sumW = Σ weights` (must be > 0).
2. For each i: `base[i] = floor(total * weights[i] / sumW)` (use 128-bit-safe math or `BigInteger` for the product).
3. `leftover = total − Σ base`.
4. Sort indices by fractional remainder descending, where the remainder of index i is the integer `(total * weights[i]) mod sumW` (no floating point). Break ties by **rotated position** `(i − rotation) mod n`, ascending, where `rotation = (first 4 bytes of SHA-256(UTF-8 bytes of tieKey), read as an unsigned big-endian integer) mod n`. Indices are in the order the participants appear in the expense (group join order).
5. Give 1 paise to the first `leftover` indices.
6. Return allocations. **Invariant: Σ result == total exactly.**

`tieKey` is the event ID, so replays always give the same result. Standard tie keys: expense shares `eventId`; itemized item i `eventId + ":item:" + i`; itemized extras `eventId + ":extras"`; original-flow pair allocation `eventId + ":flow"`.

The Python script `tools/reference_engine.py` is the executable oracle for `allocate`, balances and simplification. The Kotlin engine must produce identical outputs.

Negative totals (refunds) allocate the absolute value then negate each share.

### Split types

| Type | Inputs | Weights |
|---|---|---|
| EQUAL | participant set | 1 each |
| EXACT | amount per person | must sum to total, otherwise reject with the difference |
| PERCENT | percent per person in basis points | must sum to 10000, otherwise reject |
| SHARES | integer shares per person | the shares |
| ITEMIZED | items, each assigned to a set of people, plus extras | see below |

### Itemized with extras (proportional)

1. For each item, `allocate(item.amount, equal weights over item.people, tieKey = eventId + itemIndex)`.
2. `subtotal[p] = Σ item shares for p`.
3. `extras = Σ signed extras` (tax, tip, delivery positive; discount negative).
4. `extraShare = allocate(extras, weights = subtotal values, tieKey = eventId + "x")` (for negative extras allocate the absolute value, then negate).
5. `share[p] = subtotal[p] + extraShare[p]`.
6. Validate `Σ items + extras == total`.

People with zero subtotal get zero extras.

## 3. Entities (mutable labels)

- `Person(id, name, status = ACTIVE|ARCHIVED, mergedInto?, isMe)`
- `Group(id, name, status = ACTIVE|ARCHIVED|CLOSED, memberIds, revision, createdAt)`
- `Account(id, name, type = CASH|UPI_BANK|CARD, openingBalance)`
- `Category(id, name, icon)`

Events reference IDs only, so renames never touch history. At read time resolve `mergedInto` chains so merged people count as one.

## 4. Events (immutable)

Common fields: `id (UUID)`, `scope (PERSONAL | groupId)`, `type`, `occurredDate (local yyyy-MM-dd)`, `occurredAt (epoch ms)`, `recordedAt (epoch ms, system set)`, `enteredBy`, `currency`, `supersedes?`, `note?`.

| Type | Extra fields |
|---|---|
| EXPENSE | `total`, `description`, `categoryId`, `accountId?` (account "me" paid from), `paid: Map<Person, Long>`, `split` (type + inputs), `items?`, `extras?`, `shares: Map<Person, Long>` (**stored snapshot**), `refundOf?`, `emoji?` |
| TRANSFER | `fromAccount`, `toAccount`, `amount` |
| LENDING | `person`, `amount`, `accountId` |
| BORROWING | `person`, `amount`, `accountId` |
| SETTLEMENT | `fromPerson`, `toPerson`, `amount`, `method (CASH|UPI|OTHER)`, `accountId?` |
| VOID | `targetId` |

Rules:
- An event is **effective** unless a VOID targets it.
- **Edit** = one transaction: write VOID(old) + new event with `supersedes = old.id`.
- **Delete** = write VOID(target).
- Store both split **inputs** and computed **shares**. Replay uses the stored shares and never recomputes, so a later rounding change cannot alter history.
- A refund is an EXPENSE with `refundOf` set and negative `total`, `paid`, `shares`. Default split = original split.
- Group `revision` increments **once per user action** that writes to the group (an edit writes two rows, VOID + new event, but counts as one revision).

## 5. Balance engine (per group)

For all effective events in the group, after resolving merged people:

```
net[p] = Σ EXPENSE (paid[p] − shares[p])
       + Σ SETTLEMENT where fromPerson == p  (+amount)
       − Σ SETTLEMENT where toPerson == p    (−amount)
```

Positive = the group owes p. Negative = p owes the group.

- **Invariant:** `Σ net[p] == 0`. If a pending write would break it, reject the write.
- Balances are always derived, never persisted as authoritative data (an in-memory/rebuildable cache is fine).
- Someone both owed and owing is a single net number.

### Worked example

Abhi pays ₹1,000; Rahul's share ₹300; Aman's share ₹700; Abhi's share 0.
Net: Abhi +1000, Rahul −300, Aman −700.
Aman pays Abhi ₹500 (SETTLEMENT from Aman to Abhi): Abhi +500, Aman −200, Rahul −300. Sum is 0.

## 6. Simplification (default view)

Input: nets. Output: a list of `(from, to, amount)`.

1. `creditors = nets > 0`, `debtors = nets < 0`.
2. Sort each by absolute amount descending, ties by person ID ascending.
3. While both non-empty: take the largest creditor `c` and largest debtor `d`, `x = min(c, |d|)`, emit `d → c : x`, reduce both, drop zeros, re-sort.
4. Result has at most `n − 1` payments. (Minimum payment count is NP-hard in general. This greedy is accepted.)

**Invariant:** applying the output as settlements brings every net to exactly 0, and the output never changes any net.

Wording shown to the user: "Pay Rahul ₹840" / "Receive ₹400 from Aman".

### Original-flow view (toggle)

For each effective EXPENSE: people with `shares > paid` are deficit holders; people with `paid > shares` are surplus holders. Each deficit holder owes each surplus holder an amount allocated in proportion to surplus (use `allocate`, tieKey = eventId). Sum pairwise across expenses and net each pair (A→B minus B→A). Then apply SETTLEMENT events directly to the pair. Display only non-zero pairs.

## 7. Settlement

- A SETTLEMENT is any amount from one person to another. Partial is normal.
- Overpayment is allowed: the direction of the balance flips. Show a warning.
- Method (cash/UPI/other) is a tag only. The app never moves money.
- A group can be set to CLOSED only when every net is 0 (inactive members included).

## 8. Edits, deletes, refunds

- Editing after settlement is allowed. Show the impact first ("This changes Rahul's balance by +₹120").
- A voided event stays in history, marked deleted.
- Editing a VOID is not possible. Restore = create a new event.
- Property: VOID(e) followed by re-adding an identical event yields the same balances as before.

## 9. Membership

- Add people at any time. A person only takes part in expenses where selected.
- **Leave:** mark ARCHIVED. They remain in history and nets. A group can't close until their net is 0.
- **Delete person:** only if they have no events. Otherwise archive.
- **Merge:** set `mergedInto` on the duplicate. No events are rewritten.
- **Joins mid-way:** nothing retroactive. They appear only in newly entered expenses.

## 10. Personal ledger and the bridge

Definitions:
- **Spending** = personal EXPENSE totals + "me"'s `shares` in group EXPENSES (refunds negative). Bucketed by `occurredDate` and category.
- **Never spending:** TRANSFER, LENDING, BORROWING, SETTLEMENT, card bill payments.
- **Cash movement** (per account): personal EXPENSE and group EXPENSE where "me" paid (from `accountId`) are outflows; LENDING outflow; BORROWING inflow; SETTLEMENT where "me" is a party is outflow/inflow on `accountId`; TRANSFER moves between accounts.
- **Receivable/payable:** `net[me]` in each group, plus person-ledger balances from LENDING/BORROWING and non-group SETTLEMENTs.

Example: me paid ₹1,000 for a dinner for 4 equal.
Cash −1000; spending +250; receivable +750.

When the keeper is **not** a participant (`isMe` unset in that group), no bridge applies.

Cards: purchase is spending on its date and increases the card's liability. A TRANSFER bank→card (bill payment) reduces both, adding no spending.

## 11. Budget math

`safeToSpendToday = (monthBudget − monthSpending − upcomingRecurringThisMonth) / daysLeftInclusiveToday`, floored at 0. Per-category budgets use the same formula per category.

## 12. Export and import

- JSON file with `schema_version`, entities, and all events (including VOIDs).
- Import is a **merge by ID**: existing IDs are skipped, so importing twice is harmless.
- After import, rebuild derived state and verify invariants. Refuse and report if any fail.
- Handoff: after the new keeper imports a group, the old keeper marks it ARCHIVED.

## 13. Global invariants (the test suite's backbone)

1. Every expense's shares sum to its total.
2. Group nets sum to zero.
3. Simplification never changes any net, and returns ≤ n−1 payments.
4. VOID then re-add of an identical event gives identical balances.
5. Replaying the same effective events in any order gives identical state.
6. Personal spending never includes lending, borrowing, transfers or settlements.
7. Export then import yields identical balances and spending.
8. `allocate` is deterministic for a given `tieKey`.

Use property-based tests (Kotest `checkAll` or equivalent) for 1–5, 7, 8 with random events and people.

## 14. Required example tests

1. ₹100 split three ways: shares sum to 100, deterministic distribution.
2. Itemized ₹1,000 with ₹100 tax and ₹50 discount: proportional, exact sum.
3. Two payers on one expense: nets correct.
4. Edit after partial settlement: balances change correctly, history shows both versions.
5. Void a settlement: balances return to prior state.
6. Partial refund with a different split.
7. Archived person with nonzero net stays in simplification.
8. Overpaying a settlement flips direction and warns.
9. Cyclic debts A→B→C→A collapse to the minimum.
10. Shuffled replay of the same events: identical balances.
11. Keeper not a participant: no personal-bridge effects.
12. Card purchase then card bill payment counts spending once.
13. Merged people count as one in nets.
14. Import the same file twice: no duplicates.
