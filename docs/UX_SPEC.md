# UX_SPEC — screens, states, defaults and copy

Companion to `DESIGN_SYSTEM.md` (how it looks) and `PRD.md` (what it must do). This file defines **what is on each screen, every state it can be in, validation, and the exact wording**. If a state isn't listed, use the closest listed pattern and note it in `docs/PROGRESS.md`.

## 0. Global conventions

**Formatting**
- Currency: `₹` + Indian grouping, `₹1,23,456`. Show paise only when non-zero (`₹1,240` vs `₹1,240.50`).
- Dates: "Today", "Yesterday", then "6 Oct"; add the year only when not the current year. Month headers: "October 2026".
- Balances always carry a sign or arrow: `+₹400 ↑` (owed to you), `−₹840 ↓` (you owe). Net zero shows "Settled".
- Max amount ₹99,99,999.99. Two decimals max. Zero is not a valid amount.

**Interaction patterns**
- Creating or editing uses a bottom sheet for short forms and a full screen for long ones (group expense, itemized).
- Destructive actions (delete) never use a confirm dialog. They execute immediately with an **Undo snackbar for 6 seconds**. Archiving a group or person uses a confirm dialog because it is less obvious to reverse.
- Edits to an existing expense that change other people's balances show the **impact preview** (see S8) before saving.
- System back closes sheets first, then navigates up. Unsaved changes in a form show "Discard changes?" with *Keep editing* / *Discard*.
- Every list has an empty state. Loading is rare (local data), so show no spinner unless a screen takes over 150 ms.
- Keyboard: numeric keypad for amounts is the app's own numpad (no system keyboard). Text fields use the system keyboard with sentence capitalization.

**Permissions:** only `VIBRATE` and `USE_BIOMETRIC`. No INTERNET, no storage permissions (use the system file picker).

## 1. Defaults

**Categories (13), icon in brackets (Material Symbols):**
Chai & Snacks (local_cafe) · Meals (restaurant) · Groceries (shopping_basket) · Travel (directions_bus) · Fuel (local_gas_station) · Shopping (shopping_bag) · Bills & Recharge (receipt_long) · Rent (home) · Entertainment (movie) · Education (school) · Health (health_and_safety) · Gifts & Treats (card_giftcard) · Other (more_horiz).
Users can add, rename, hide and reorder categories. Hiding keeps old data intact. Default group-expense category: Meals.

**Accounts:** Cash and UPI / Bank are created at first launch. Cards are added by the user. Default account for a new entry = last used.

**Settings defaults:** Sounds on · Haptics on · Reduce motion off (also follows the system setting) · Theme follows system · App lock off · No budget set.

**First-launch data:** one person, the user ("me"), named during onboarding. No sample data.

## 2. Screens

### S1 Onboarding
- Slides (swipe or Next): 1 "Your money stays on your phone. No account, no internet." 2 "Track your own spending and shared money in one place." 3 "See what you actually spent, not just what you paid."
- Then a setup screen: **Your name** (required, 1-30 chars), **Monthly budget** (optional), **Opening balances** for Cash and UPI/Bank (optional).
- States: Skip is available on slides. The setup screen's *Start* is disabled until the name is valid.
- Ends on Home with the first-run empty state.

### S2 Home (My Money)
- **Hero card (Espresso):** month name with prev/next arrows, "Spent this month" amount (odometer), budget bar with "₹X left" and "Safe to spend today ₹Y".
- Below: **Receivable/payable strip** ("Owed to you ₹1,680 · You owe ₹200", tap opens People), **Category breakdown** (top 5 plus Other, bar list), **Recent transactions** (last 5, "See all").
- States:
  - *First run (no data):* hero shows ₹0, the strip and breakdown are hidden, an illustration with "Add your first expense" and a pointer to the FAB.
  - *No budget:* the bar is replaced by a chip "Set a monthly budget".
  - *80%+ of budget:* bar turns Clay and the nudge card appears once per month (dismissible).
  - *Over budget:* bar turns Brick, text "Over by ₹X".
  - *Past month:* read-only labels "September 2026", "Safe to spend" is hidden.

### S3 Quick add (bottom sheet from the FAB)
- Top segmented control: **Expense · Lent · Borrowed · Transfer**.
- **Expense:** big amount display, numpad, category chips (horizontal, last used first), account chip, date chip (Today / Yesterday / pick), optional note (≤ 80 chars), Save (Emerald).
- **Lent / Borrowed:** amount, person picker (existing or "+ New person"), account chip, date, note.
- **Transfer:** amount, From account, To account, date.
- Validation: Save disabled until amount > 0 and (for Expense) a category is chosen. Transfer needs two different accounts.
- Saving: sheet closes with the coin-drop animation. A snackbar is not needed (the new row visibly appears).
- Edit mode (opened from a transaction): title "Edit expense", pre-filled, **Delete** link at the bottom, **History** link opens the edit trail.

### S4 Transactions
- Reached from "See all". Grouped by day with a daily total. Filter chips: Month, Category, Account, Type. Search by note and category name.
- Row: category icon, note (or category name), account, amount. Swipe left to delete (Undo snackbar). Tap opens detail.
- Detail: all fields, the edit trail ("₹2,000 → ₹1,800, edited 3 Oct"), and Edit/Delete.
- Empty state (no matches): "Nothing here. Try a different filter."
- Deleted items are hidden unless the "Show deleted" filter is on (shown struck through with a "deleted" tag).

### S5 Groups list
- Cards: group name, member avatars (max 4 + "+N"), your net ("You're owed ₹6,070" / "You owe ₹200" / "Settled"), or "₹13,700 spent" if you're not a participant. Archived groups sit in a collapsed section at the bottom.
- Empty state: "No groups yet. Create one for a trip, a flat or a night out." with a Create group button.

### S6 Create group
- **Name** (required, ≤ 30 chars). **Members:** add from existing people or type a new name. At least 2 members. **Who are you?** radio list of members plus "I'm just keeping this group (not in the splits)". The default is you.
- Save is disabled until the name is valid and there are ≥ 2 members (≥ 2 other members when you're not participating).
- Duplicate member names show "That name is already in this group".

### S7 Group detail
- **Header (Espresso):** your net with sign/arrow, or total spent when you're not a participant; revision number in small text ("Rev 7").
- **Balances tab:** toggle *Simplified* (default) / *Original flow*. Each person: avatar, name, net badge. Archived members show "left the group". Primary button **Settle up** (disabled when everyone is settled).
- **Expenses tab:** by date. Row: emoji/category icon, description, "Paid by Abhi" (or "Paid by 2 people"), total, and a chip with your share. "edited" tag opens the trail. Toggle "Show deleted".
- **Summary tab:** live preview of the shareable summary and a Share button.
- FAB: add expense. Overflow menu: Add member, Rename, Export this group, Archive group, Close group (enabled only when all nets are 0).
- States: *Empty group* ("No expenses yet. Add the first one." plus FAB pulse), *All settled* (green check header "All square"), *Archived* (read-only banner "Archived. Restore to edit").

### S8 Add / edit group expense (full screen)
Order of fields: Amount → Description → Category → Date → **Paid by** → **Split** → preview → Save.
- **Paid by:** member chips. One tap selects a single payer (full amount). Selecting more than one reveals an amount field per payer. Account chip appears only if "me" is a payer.
- **Split** segmented: Equal · Exact · % · Shares · Itemized.
  - *Equal:* checkboxes for members (default all active). At least one required.
  - *Exact:* amount field per person.
  - *%:* percent field per person.
  - *Shares:* stepper per person (1, 2, 3…).
  - *Itemized:* opens S8b.
- **Preview** rows (avatar, name, share amount) update live. A **remaining indicator** shows "₹40 left to assign" (Brick) or "All assigned ✓" (green).
- Save is disabled until all validations pass.
- **Editing an existing expense:** after tapping Save, if any balance changes, show a dialog "This changes balances" listing people with signed changes (e.g., "Rahul +₹50"). Buttons: *Save* / *Cancel*.

**S8b Itemized:** list of items (label, amount, who shared it as chips). "+ Add item". Below: **Extras** chips: + Tax, + Tip, + Delivery, − Discount, + Custom (label + signed amount). The total is **computed** (items + extras) and shown at the top. A "How extras are shared: proportionally" note explains the rule. The Done button returns to S8 with the preview filled.

### S9 Settle up
- List of suggested payments (simplified by default, with the same toggle as S7). Each card: "Pay Rahul ₹840" or "Receive ₹400 from Aman" (phrased from your perspective if you're a participant, otherwise "Sameer → Abhi ₹2,280").
- Tap a card → **Record payment** sheet: From, To, Amount (prefilled, editable), Method chips (Cash / UPI / Other), Date, Account (only when you're a party), Save.
- **Overpay:** if amount exceeds what's owed, show inline: "This is ₹200 more than Rahul owes. After this, the balance flips." Save stays enabled.
- After each save: balance animates. When all nets reach 0, show the **All square** celebration and offer "Close group".

### S10 People
- List of people with combined net (ledger + groups). Tap opens Person detail.
- **Person detail:** per-group breakdown plus ledger balance, a timeline of events, buttons **Settle / Lent / Borrowed**, overflow: Rename, Merge into another person, Archive.
- Merge: pick the person to keep, confirm "Treat 'Rahul K' as 'Rahul' everywhere? Nothing is deleted."
- Archive person: confirm; allowed with a non-zero balance (they remain in nets).
- Empty state: "People appear here when you add them to a group or lend or borrow money."

### S11 Insights
- Cards: *This month vs last* (difference with arrow), *Top categories* (ramp bar), *Small payments* ("₹1,240 in UPI payments under ₹100"), *Daily trend* (line).
- Empty state until ≥ 7 days of data: "Add a few more expenses and insights will show up here."

### S12 Settings
Sections: **Feedback** (Sounds, Haptics, Reduce motion), **Appearance** (Theme), **Security** (App lock), **Money** (Categories, Accounts, Monthly budget, Recurring), **Data** (Export, Import, Export reminders), **About** (version, licenses, privacy statement: "All data stays on this device").

### S13 Share preview
- Receipt-style image plus text version. Toggle: include itemized expenses (off by default). Buttons: Share image, Share text, Copy text.

### S14 Export / Import
- *Export all* or *Export this group* → system file picker → success snackbar "Backup saved".
- *Import:* pick file → result summary "12 new entries added, 3 already here". If validation fails: "This file doesn't add up (balances don't match), so nothing was imported."
- A reminder card appears after every 25 new events if no export in 30 days: "Back up your data. It lives only on this phone."

### S15 App lock
- On open (and after 1 minute in the background) show the biometric prompt. Fallback to device credential. Failure copy: "Unlock to see your money."

## 3. Copy library

**Validation**
- Amount: "Enter an amount greater than ₹0"
- Nobody selected: "Pick at least one person to split with"
- Exact split: "₹40 left to assign" / "₹10 over the total"
- Percent: "Percentages add up to 98%. They need to reach 100%"
- Paid amounts: "Paid amounts must add up to ₹1,200 (₹200 missing)"
- Itemized: "Every item needs at least one person"
- Name: "Give it a name" / "That name is already in this group"

**Snackbars and toasts**
- "Expense deleted" [Undo] · "Payment deleted" [Undo] · "Backup saved" · "Group archived" [Undo]

**Reaction messages**
- 80% budget: "You're at 80% of this month's budget, about ₹{x} a day left."
- Over budget: "Over by ₹{x} this month. It happens. Tomorrow is a fresh day."
- Streak: "{n} days in a row!"
- All square: "All square 🎉 Nobody owes anything."
- Small payments: "₹{x} went on tiny UPI payments this month 🐀💸"
- Month vs last: "You spent ₹{x} {more|less} on {category} than last month."

**Empty states (headline / body / action)**
- Home: "Nothing yet" / "Add your first expense to get started." / Add expense
- Groups: "No groups yet" / "Create one for a trip, a flat or a night out." / Create group
- Group expenses: "No expenses yet" / "Add the first one." / Add expense
- People: "No one here yet" / "People show up when you add them to a group or lend or borrow money." / —

**Share summary text template**
```
🧾 {Group name} · Rev {n} · {date}

Balances
{Name} is owed ₹{x}
{Name} owes ₹{y}
…

To settle up
{From} → {To} ₹{amount}
…

Shared from Ledgr
```
When "include expenses" is on, append a list: `{date} · {description} · ₹{total} · paid by {payer}`.

## 4. Verification on devices

- Use the **Pixel 10 emulator** for layouts, themes (light/dark), font scale 100%/200%, and navigation flows.
- Use the **physical phone** for sound, haptics, performance (60 fps), the biometric prompt and share sheet. The emulator can't judge these reliably.
- Run the Goa-trip golden scenario (`GOLDEN_SCENARIOS.md` G1) end to end through S6 → S8 → S7 → S9 → S13 on both.
- Tap-count checks: personal expense = FAB + amount digits + category + Save; equal group expense = group FAB + amount + description + Save (payer defaults to "me" and everyone is selected).
