# GOLDEN_SCENARIOS — expected values for the engine

Every number here was computed by `tools/reference_engine.py` (run it to reproduce). The Kotlin `:engine` must match **exactly**, and each scenario becomes a named unit test (`golden_G1_goaTrip`, etc.). Amounts are in rupees unless marked paise. Net sign: **+ = is owed money, − = owes money**.

Group join order used everywhere unless stated: Abhi (me), Rahul, Aman, Rohit, Sameer.

---

## G1 — Goa trip (5 people, multiple payers, subset participation, exact split)

| # | Expense | Paid by | Split | Shares |
|---|---|---|---|---|
| E1 | Hotel ₹6,000 | Abhi | Equal, all 5 | 1,200 each |
| E2 | Food ₹2,400 | Rahul | Equal, all 5 | 480 each |
| E3 | Fuel ₹3,000 | Abhi | Equal, Abhi/Rahul/Aman/Rohit (Sameer came by train) | 750 each |
| E4 | Tickets ₹1,500 | Aman | Equal, all 5 | 300 each |
| E5 | Gifts ₹800 | Rohit | Exact: Abhi 200, Rohit 300, Sameer 300 | as given |

**Expected totals paid:** Abhi 9,000 · Rahul 2,400 · Aman 1,500 · Rohit 800 · Sameer 0 (sum 13,700)
**Expected shares:** Abhi 2,930 · Rahul 2,730 · Aman 2,730 · Rohit 3,030 · Sameer 2,280 (sum 13,700)

**Expected nets:** Abhi **+6,070** · Rahul **−330** · Aman **−1,230** · Rohit **−2,230** · Sameer **−2,280** (sum 0)

**Simplified payments (default view), in this order:**
1. Sameer → Abhi ₹2,280
2. Rohit → Abhi ₹2,230
3. Aman → Abhi ₹1,230
4. Rahul → Abhi ₹330

**Abhi's personal bridge (keeper is "me" and a participant):**
- Personal spending from this group: **₹2,930**
- Cash movement: **−₹9,000** (what he paid)
- Receivable: **+₹6,070**

**G1b — Sameer pays Abhi ₹1,000 (cash settlement):**
Nets: Abhi **+5,070** · Rahul −330 · Aman −1,230 · Rohit −2,230 · Sameer **−1,280**.
Simplified order: Rohit→Abhi 2,230 · Sameer→Abhi 1,280 · Aman→Abhi 1,230 · Rahul→Abhi 330.
Abhi's personal spending is **unchanged** at ₹2,930. Cash +₹1,000, receivable now ₹5,070.

**G1c — Sameer is archived (left the group) with −₹1,280 still owing:**
He still appears in nets and simplification, and the group cannot be set to CLOSED until his net is 0.

---

## G2 — Cycle collapses (simplification value)

| # | Expense | Paid by | Split |
|---|---|---|---|
| E1 | ₹1,000 | Rahul | Equal Abhi/Rahul (500 each) |
| E2 | ₹1,400 | Aman | Equal Aman/Rahul (700 each) |
| E3 | ₹600 | Abhi | Equal Abhi/Aman (300 each) |

**Original-flow view (toggle), 3 pairs:** Abhi owes Rahul ₹500 · Rahul owes Aman ₹700 · Aman owes Abhi ₹300.
**Nets:** Abhi −200 · Rahul −200 · Aman **+400**.
**Simplified (2 payments):** Abhi → Aman ₹200 · Rahul → Aman ₹200.

---

## G3 — Rounding is exact and deterministic (values in **paise**)

`allocate(10000, [1,1,1], tieKey)`; the extra paisa goes to the participant at the first rotated position:

| tieKey | Result (paise) |
|---|---|
| `g3-a` | [3333, 3333, 3334] |
| `g3-b` | [3333, 3334, 3333] |
| `g3-c` | [3333, 3334, 3333] |
| `g3-d` | [3333, 3333, 3334] |

`allocate(1000, [1,1,1], "g3-a")` = [333, 333, 334].
`allocate(1001, [3,2,5], "g4-r")` = [300, 200, 501] (the largest remainder, 5/10, gets the extra paisa).
Properties: result sums to the total exactly; same tieKey always gives the same result; negative totals give the negated allocation of the absolute value.

---

## G4 — Itemized bill with tax and discount (proportional extras)

Bill paid by Abhi. Items: Abhi ₹300, Rahul ₹200, Aman ₹500. Extras: tax **+₹100**, discount **−₹50** (net extras +₹50). Total ₹1,050.

- Extras allocated proportionally to subtotals 3:2:5 → +₹15, +₹10, +₹25
- **Shares:** Abhi **₹315** · Rahul **₹210** · Aman **₹525** (sum 1,050)
- **Nets:** Abhi paid 1,050 − 315 = **+735** · Rahul **−210** · Aman **−525**

---

## G5 — Two payers on one expense

Dinner ₹1,200, Abhi paid 700 and Rahul paid 500, equal among Abhi, Rahul, Aman (400 each).
**Nets:** Abhi **+300** · Rahul **+100** · Aman **−400**.
**Simplified:** Aman → Abhi ₹300, Aman → Rahul ₹100.
**Original flow:** the same two payments (Aman is the only deficit holder, allocated by surplus 300:100).

**G5b — "All square":** record Aman → Abhi ₹300 and Aman → Rahul ₹100. All nets are exactly 0, the settle-up screen shows the celebration, and the group may be CLOSED.

---

## G6 — Edit after a partial settlement

1. E1: Dinner ₹2,000 paid by Abhi, equal among Abhi, Rahul, Aman, Rohit (500 each). Group revision 1.
2. Settlement: Rahul → Abhi ₹300. Revision 2.
   Nets: Abhi **+1,200** · Rahul **−200** · Aman −500 · Rohit −500.
3. Edit E1 to ₹1,800 (450 each). One user action: VOID(E1) + new event with `supersedes = E1`. **Revision 3** (not 4).
   Nets: Abhi **+1,050** · Rahul **−150** · Aman **−450** · Rohit **−450** (sum 0).
4. History shows "Dinner: ₹2,000 → ₹1,800, edited". The edit impact preview before saving shows Abhi −150, Rahul +50, Aman +50, Rohit +50.

**G7 — Void the settlement instead (from step 2):** nets return to Abhi **+1,500** · Rahul −500 · Aman −500 · Rohit −500.

---

## G8 — Partial refund with a different split

E1: Dinner ₹2,000 paid by Abhi, equal among 4 (500 each).
Refund event (`refundOf = E1`): total **−₹400**, paid Abhi **−400**, shares Rahul **−200**, Aman **−200**.
**Nets:** Abhi **+1,100** · Rahul **−300** · Aman **−300** · Rohit **−500** (sum 0).
Abhi's personal spending stays ₹500; his cash movement is −₹2,000 + ₹400 = **−₹1,600**.

---

## G9 — Overpaying a settlement

From E1 in G6 (nets Abhi +1,500, others −500): Rahul pays Abhi ₹700.
**Nets:** Abhi **+800** · Rahul **+200** (direction flipped) · Aman −500 · Rohit −500.
The UI shows a warning at entry time ("This is ₹200 more than Rahul owes"), but the entry is allowed.

---

## G10 — Personal bridge and settlement

Abhi pays ₹1,000 for a dinner shared equally by 4 (250 each) from his UPI account.
- Spending **+₹250** (this month, category of the expense)
- UPI account **−₹1,000**
- Receivable **+₹750**

Then Aman pays Abhi ₹250 by UPI: UPI account **+₹250**, receivable **₹500**, spending still **₹250** (a settlement is never spending).

---

## G11 — Card purchase and bill payment

Credit card account, opening balance 0.
1. Personal expense ₹1,000 on the card: spending **₹1,000**, card balance **−₹1,000** (liability).
2. Transfer UPI → card ₹1,000 (bill payment): UPI **−₹1,000**, card **₹0**, spending **still ₹1,000** (counted once).

---

## G12 — Keeper is not a participant

Abhi keeps a group (hostel trip) but does not take part in any expense (not "me" in that group).
Personal spending, accounts and receivables on Abhi's phone are **unchanged** by every event in that group. Group nets, simplification and share summaries work as usual.

---

## G13 — Merged people

Aman pays ₹500 equal Aman/Rahul (250 each), then ₹500 equal Aman/"Rahul K" (250 each).
Before merge: Rahul −250 · Rahul K −250 · Aman +500.
After setting `Rahul K.mergedInto = Rahul`: Rahul **−500** · Aman **+500**, and no event rows are modified.

---

## G14 — Safe to spend today

Month budget ₹9,000, spent so far ₹3,500, upcoming recurring this month ₹500, today is 6 October (days left inclusive of today = 26).
Result = floor(500,000 paise ÷ 26) = **19,230 paise = ₹192.30**. If spent ≥ budget − recurring, the result is ₹0.

---

## G15 — Replay, void/re-add and export/import

- Shuffling the order of effective events and recomputing yields identical nets, spending and account balances (property test over random event sets).
- VOID(X) followed by a new event identical to X (new ID) restores the exact same nets as before the VOID.
- Export → import into a fresh database gives identical nets and spending. Importing the same file a second time adds **zero** events.

---

## Using these in tests

- Build each scenario from events with fixed IDs, and fixed tieKeys as above.
- For G1, E1–E5 use tieKeys `g1-e1`…`g1-e4`; E5 is exact so needs none. Equal-split expenses here divide exactly so the tieKeys do not change the outcome.
- Assert shares, nets, simplified payments (order matters), personal snapshot values, and the revision number where stated.
- Regenerate expected values with `python3 tools/reference_engine.py` if the spec changes, and update this file in the same commit.
