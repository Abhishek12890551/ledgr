# VALIDATION_KIT — is this worth building, and for whom?

Purpose: replace guesses with evidence. Do Part A (a few hours) and Part B (a week) **in parallel with building the engine (M0-M1)**. Part C runs once a v1 build exists. Part D turns the evidence into a decision.

The ratings and thresholds below are heuristics to force a clear decision, not statistics. Be honest when scoring.

## Part A — Competitor hands-on test (2-3 hours)

Install each app and run the same scenario. Apps to test: Splitwise, Lekhhaa, Hisaab, Money Manager, one more of your choice (e.g., the UPI app expense feature you use). Verify that each app still exists and what it currently does. The competitor claims in `PRD.md` came from a quick pass.

**Scenario (from `GOLDEN_SCENARIOS.md` G1):** 5 people on a trip. Enter E1-E5. Then record Sameer paying ₹1,000. Then check what each app shows.

For each app, fill this table:

| Question | App 1 | App 2 | App 3 | App 4 | App 5 |
|---|---|---|---|---|---|
| Time to enter E1-E5 (minutes) | | | | | |
| Does it need an account / internet? | | | | | |
| Do friends need the app to take part? | | | | | |
| Handles E3 (only 4 of 5 share fuel) and E5 (exact split)? | | | | | |
| Shows Abhi +₹6,070 and the right simplified payments? | | | | | |
| **Personal spending after the trip: ₹2,930, or ₹9,000?** (the bridge test) | | | | | |
| Does cash paid on behalf of others pollute personal totals? | | | | | |
| Edit an expense after a settlement: is history visible? | | | | | |
| Shareable summary for non-users? | | | | | |
| Ads / paywall / limits on the free tier? | | | | | |
| What annoyed you? | | | | | |
| What did it do better than the plan for Ledgr? | | | | | |

**Key output:** write 3 sentences. (1) Which app comes closest. (2) Exactly where it falls short on the bridge test. (3) Whether that gap would matter to a student on a trip.

## Part B — Interviews (8-10 people, 20 minutes each)

Who: hostel mates, classmates, flatmates, friends who go on trips. Mix people who use Splitwise and people who don't. Not family doing you a favor. Casual conversation, in person or by call. Don't pitch the app first.

**Opening (1 min):** "I'm researching how people handle money when they spend with friends. No right answers, and I'm not selling anything."

**Questions (ask in this order; follow interesting answers):**
1. Tell me about the last time you shared expenses with friends (trip, flat, dinner). What happened?
2. How did you keep track? (Notes app, memory, WhatsApp, Splitwise, nothing?)
3. What went wrong or felt annoying? Did anyone end up paying more or never get paid back?
4. How did you settle in the end? How long did it take?
5. Do you track your own spending? How? When did you last stop, and why?
6. When you pay for the group, does it mess up how you see your own spending?
7. Have you tried Splitwise or similar? What made you stop or keep going?
8. If your friends don't use the app, how does that work for you?
9. How do you feel about an app that keeps all data only on your phone, with no account? (Good, bad, don't care?)
10. What would make you switch from what you use now? What would make you refuse?

**Close (1 min):** "Is there anything I should have asked?" Then ask if they'd try an early version in a few weeks.

**After each interview, fill in:**

| Field | Notes |
|---|---|
| Who (initials, context: hostel/flat/trips) | |
| Current tool | |
| Pain points mentioned **unprompted** (tick from the PRD list: forgot small expenses, unclear where money went, confusion on who owes whom, friends not on app, settlement hassle, group/personal mix-up, none) | |
| Most vivid story (1-2 lines) | |
| Reaction to offline/private (1-5) | |
| Would try an early version? (yes/maybe/no) | |
| Quotes worth keeping | |

Rules: don't lead ("Wouldn't it be nice if…"), ask about past behavior over hypothetical interest, and record what they did, not what they say they'd do.

## Part C — Prototype test (once v1 exists, 5 friends, 15 minutes each)

Give them the app on your phone (or an installed debug build). Don't explain it. Ask them to:
1. Add a personal expense of ₹120 for chai.
2. Create a group "Trip" with you and two friends.
3. Add a dinner where you paid ₹900 and it's shared equally.
4. Add a bill where one person ordered more (itemized with tax).
5. Work out who should pay whom and record one payment.
6. Share the summary and read it as if you were a friend who doesn't have the app.

**Measure:** time per task, taps, hesitations, errors, and what they said out loud. Afterwards ask: "What was confusing?", "What did you expect to happen when you tapped ___?", "Would you use this for your next trip?"

Also check **comprehension of the summary**: show the shared summary to a friend who has never seen the app and ask "What do you owe, and to whom?" They should answer correctly without help.

## Part D — Decision rubric

Fill after Parts A and B (add Part C when ready).

| Signal | Heuristic threshold for "yes" | Your score |
|---|---|---|
| Interviewees describing an unresolved pain from the PRD list unprompted | ≥ 5 of 10 | |
| People who have actually abandoned a tool for this and can say why | ≥ 3 | |
| People who would try an early version | ≥ 5 | |
| People bothered by "friends must install the app" | ≥ 4 | |
| Competitor bridge test: no tested app gets it right | Yes | |
| Offline/private rated 4+ by | ≥ 4 | |

**Outcomes:**
- **Mostly yes:** build v1 as planned, aimed at your validated segment (probably students and trip groups). Consider the Play Store (see `RELEASE.md`).
- **Mixed:** build v1 for yourself and a few friends, keep it small, don't invest in growth.
- **Mostly no:** treat it as a **portfolio and learning project**. Still valuable for engineering depth, and the on-device ML work (`ML_PLAN.md`) is the part most relevant to your PhD applications. Don't spend weeks on store polish.

Write the decision and the evidence in `docs/PROGRESS.md` so you can revisit it later.
