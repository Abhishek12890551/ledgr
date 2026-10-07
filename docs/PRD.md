# PRD — Ledgr (working title)

Status: v1 spec, ready to build. Companion docs: `ENGINE_SPEC.md` (money logic), `DESIGN_SYSTEM.md` (UI), `TECH_STACK.md` (architecture).

## 1. Vision

A fully offline, private money companion that handles **your money** and **shared money** in one place, and keeps them consistent: what you *paid* is never confused with what you actually *spent*.

## 2. Problem

> "I know I spent money, but I don't know where it went. And when I'm with friends, tracking who owes whom is a headache."

Existing apps (Splitwise, Lekhhaa, Hisaab, Money Manager, Wallet, etc.) either do group splitting well or personal tracking well. Where both exist, the link between them is weak, so personal analytics get polluted by money paid on behalf of others. Most also require a connection and an account, and want every friend to install the app.

Competitor claims are leads to verify by hands-on testing, not facts to rely on.

## 3. Users

| Persona | Description | Needs |
|---|---|---|
| **Student / young professional** | Tracks small daily spends (₹20–₹500), cash + UPI | Fast entry, honest "where did it go" insights |
| **Keeper** | Person who holds a group (trip organizer, flat manager). Can be anyone, including someone not in any split | Fast group entry, correct balances, easy settle-up, shareable summaries |
| **Participant** | Friend in a group who does **not** use the app | Receives a clear read-only summary over WhatsApp or similar |

## 4. Goals and non-goals

**Goals (v1)**
1. Personal tracking that is accurate even when group money is involved.
2. Group splitting with all common split types and a correct, explainable settle-up.
3. 100% offline. No account, no server, no analytics SDK. Data never leaves the phone unless the user exports or shares it.
4. Fast entry: a personal expense in ≤ 3 taps and ≤ 5 seconds; a typical equal-split group expense in ≤ 5 taps.
5. A polished, delightful feel (motion, sound, haptics) that is optional and accessible.

**Non-goals (v1)**
- Cloud sync, accounts, multi-device merge (the keeper model replaces this)
- Moving money or launching UPI payments
- Reading SMS or notifications automatically
- Natural-language or voice entry, on-device ML (planned for v1.5/v2, see roadmap)
- Multi-currency
- Participant-submitted expenses
- iOS

## 5. Product principles

1. **Never confuse cash movement with actual spending.** (See `ENGINE_SPEC.md` §10.)
2. **Events are immutable.** Edits and deletes create new events. History is always visible.
3. **Derived, never stored:** balances and analytics are recomputed from events.
4. **Say what to do:** "Pay Rahul ₹840", not "You owe ₹1,240".
5. **Offline is the only mode.** There is no "online" state to handle.
6. **Delight must never block speed.** Animations are skippable; sounds and haptics are optional.

## 6. Scope (MoSCoW)

**Must**
- Personal: add/edit/delete expense, categories, accounts (cash/UPI-bank/card), history, monthly summary and breakdown, monthly budget
- Person ledger: lending, borrowing, settlements outside groups
- Groups: create, add participants, add expense (equal, exact, percent, shares, itemized with extras), multiple payers, balances, simplified settle-up with "original flow" toggle, record settlement, edit/void with visible history
- Bridge: the keeper's personal spending reflects their **share** of group expenses
- Share summary of a group as text and image
- Export/import backup file; group handoff to another keeper
- Material 3 UI with the design system, animations, sound, haptics, dark mode

**Should**
- Insights ("₹730 more on food than last month", "₹1,240 on UPI payments under ₹100")
- Recurring personal expenses
- Biometric app lock
- Refunds

**Could**
- Home-screen widget for quick add
- Expense emoji tags

**Won't (v1)**
- Everything in non-goals

## 7. Features and acceptance criteria

### F1 Quick add (personal)
- Numpad-first screen; amount is the largest element.
- Category chip row, account selector (defaults to last used), optional note and date (defaults to today).
- **AC:** with defaults, Save is reachable after typing the amount and tapping one category. Saved expense appears in history immediately with an animation.

### F2 Edit and delete
- Edit opens the same form pre-filled. Delete shows an undo snackbar for 6 seconds.
- **AC:** after edit/delete, history shows the change trail ("₹2,000 → ₹1,800, edited 3 Oct"). Balances and totals update instantly.

### F3 Accounts and transfers
- Accounts: cash, UPI/bank, card. Opening balance optional.
- Transfer moves money between accounts and is never counted as spending. Paying a card bill is a transfer.
- **AC:** a card purchase counts once as spending, and the later card payment adds no spending.

### F4 Budgets and monthly view
- Monthly total budget and optional per-category budgets.
- Home shows month spent, remaining, and "safe to spend today" = (budget − spent − upcoming recurring) ÷ days left.
- **AC:** values update after each entry. Budget at 80% and 100% trigger reaction moments (see design).

### F5 Person ledger (lending/borrowing)
- "I gave Rahul ₹500" / "I borrowed ₹1,000 from Aman" create lending/borrowing events that are not expenses.
- Person page shows net balance and event list. Settlement records repayment, partial allowed.
- **AC:** lending reduces account cash and creates a receivable without changing monthly spending.

### F6 Groups
- Create a group with a name and participants (people can be added any time).
- The keeper sets which participant is "me", or none.
- **AC:** a group with zero expenses shows an empty state with a single clear action.

### F7 Group expense entry
- Fields: amount, description, category, date, who paid (one or more, amounts), split type and inputs, extras (tax/tip/delivery/discount) for itemized.
- Validation errors say exactly what's off ("₹40 left to assign").
- **AC:** every split type yields shares that sum exactly to the total (integer paise, largest-remainder rounding).

### F8 Balances and settle-up
- Group screen shows each person's net (positive = is owed, negative = owes).
- Simplified view is **default**. A toggle shows the original flow of money (who owes whom per expense, netted pairwise).
- Settle-up screen lists suggested payments and lets the keeper record a payment (amount editable, method tag cash/UPI/other).
- **AC:** simplification never changes anyone's net balance and produces at most n−1 payments.

### F9 History and trust
- Every expense shows who added it, when, and its split. Edits and voids are visible.
- Each group has a **revision number** that increments on every event and appears on shared summaries.
- **AC:** voiding then re-adding an identical event returns identical balances.

### F10 Share summary
- Generate text and a rendered image: group name, revision, date, per-person net, suggested payments, itemized expense list (optional).
- Shared through the Android share sheet. No app is needed to read it.
- **AC:** a participant can understand what to pay from the summary alone.

### F11 Backup, restore, handoff
- Export everything or one group as a versioned JSON file via the system file picker. Import merges by event ID (idempotent).
- Handoff: export group, import on the new keeper's phone, mark the old copy archived.
- **AC:** exporting then importing into a fresh install reproduces identical balances.

### F12 Interaction layer
- Animations, sound effects, haptics and reaction moments per `DESIGN_SYSTEM.md`.
- Settings toggles: sounds, haptics, reduce motion (also follows the system animation scale).
- **AC:** with all three off, the app is fully functional and no feedback is lost (visual state still changes).

## 8. Key flows

1. **Add personal expense:** FAB → numpad → category → Save → coin animation → back to Home with updated totals.
2. **Add group expense:** Group → "+" → amount → payer chip(s) → split type (equal default, all participants selected) → Save.
3. **Settle up:** Group → Settle → pick suggested payment → confirm amount/method → balance animates → at zero show "All square" celebration.
4. **Share summary:** Group → Share → preview → share sheet.
5. **Handoff:** Group menu → Export → send file → new keeper imports → old keeper archives.

## 9. Non-functional requirements

- **Offline:** the manifest must not declare the INTERNET permission.
- **Performance:** cold start < 1.5 s on a mid-range phone; adding an entry < 100 ms to persist; smooth 60 fps animations on a ~₹10k device. Handles 50,000 events without noticeable lag.
- **Privacy:** no analytics or crash SDKs that transmit data; Android auto-backup disabled; optional biometric lock.
- **Accessibility:** WCAG AA contrast, 48dp touch targets, TalkBack labels, reduce-motion support, no information conveyed by color or sound alone.
- **Compatibility:** Android 8.0 (API 26) and above.
- **Localization:** English UI first; Indian number grouping (₹1,23,456); structure strings for a later Hindi translation.
- **Data integrity:** the app refuses to save any state that breaks the invariants in `ENGINE_SPEC.md` §13.

## 10. Success measures (self-assessed, no telemetry)

- Add-expense task time (≤ 5 s personal, ≤ 15 s group) on a real device
- The Goa-trip scenario, run end to end, matches hand-calculated balances
- All invariant and edge-case tests pass
- At least 5 friends or hostel-mates can read a shared summary and correctly say what they owe

## 11. Roadmap

| Version | Content |
|---|---|
| **v1** | Everything in Must/Should above |
| **v1.5** | On-device categorizer (description → category) trained on the user's own entries |
| **v2** | On-device free-text parser (English + Hinglish) producing the same structured entry the manual form creates; participant claims by QR/file; recurring shared expenses with per-person weights; multi-currency |

## 12. Risks

| Risk | Mitigation |
|---|---|
| Keeper loses phone, so the group is lost | Prominent export reminder after N events; one-tap backup |
| Participants dispute entries | Itemized summaries with revision numbers; keeper edits with visible history |
| Differentiation is thin vs. existing apps | Validate with real users before investing beyond v1; lean on correctness and the bridge |
| Rounding or logic bugs break trust | Pure engine module, property-based tests, invariants enforced at save time |
| Sound/animation annoys users | Subtle by default, easy toggles, respect silent mode and system animation settings |

## 13. Glossary

- **Keeper:** the person whose phone holds a group.
- **Participant:** a named person in a group, not necessarily an app user.
- **Event:** an immutable record of something that happened.
- **Net balance:** what a person has paid minus what they owe, plus settlements.
- **Bridge:** logic that converts group events into the keeper's personal spending, receivables and payables.
