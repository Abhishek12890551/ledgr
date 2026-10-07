# DESIGN_SYSTEM — Ledgr (working title)

Platform: Android, Jetpack Compose, Material 3 as the base with a fully custom theme. Light and dark themes are both first-class.

Feel: warm, grounded, confident, a little playful. Earthy neutrals with one fresh green. Money screens are clean and numeric. Delight appears at moments of completion, not everywhere.

## 1. Palette (from the project's Coolors palette)

Source: https://coolors.co/00a676-f7f9f9-e0d0c1-a76d60-601700

| Name | Hex | Role |
|---|---|---|
| **Emerald** | `#00A676` | Accent, the brand "go" color |
| **Mist** | `#F7F9F9` | Dominant background |
| **Sand** | `#E0D0C1` | Secondary containers |
| **Clay** | `#A76D60` | Secondary mid-tone |
| **Espresso** | `#601700` | Secondary dark anchor, text |

Only these five are brand colors. Everything below marked *derived* is a lightened/darkened shade of them, added only for contrast, dark mode and charts.

## 2. The 60-30-10 rule

| Share | Role | Light theme | Dark theme (*derived*) |
|---|---|---|---|
| **60% Dominant** (backgrounds, cards, whitespace) | Neutral | Mist `#F7F9F9` background, White `#FFFFFF` card surface, Sand-light `#F1E9E1` (*derived*) for subtle fills | background `#170B07`, surface `#21120C` |
| **30% Secondary** (hero card, top bar, selected nav, chips, containers, icons, chart ramp, text) | Warm earth family | Espresso `#601700` (hero card, headings, selected nav, text), Sand `#E0D0C1` (chips, containers, bottom bar), Clay `#A76D60` (icons, secondary buttons, chart mid-tone) | Espresso-lift `#34211A` containers, Sand `#E0D0C1` for secondary text, Clay-light `#C98F82` for icons |
| **10% Accent** (the one primary action, key highlight) | Emerald | `#00A676`, content on it `#1A0900` | `#00A676`, content on it `#1A0900` |

### Text
- Primary: Espresso `#601700` on Mist/White (about 12:1 contrast). Dark theme: Mist `#F7F9F9`.
- Secondary: `#7A5A50` (*derived*, about 5.8:1 on Mist). Dark theme: Sand `#E0D0C1`.
- Text/icons on Emerald: `#1A0900` (*derived* near-black espresso, about 6:1). Never white on Emerald (only about 3:1).
- Clay `#A76D60` has about 4:1 on Mist, so use it only for icons, large text and fills, never for small body text.

### Semantic colors (outside the ratio, only for money direction and state)

| Meaning | On Mist/White (light) | On Espresso hero or dark theme |
|---|---|---|
| Is owed to you / positive | `#007A55` (*derived* from Emerald, about 5:1) | `#3DDBA8` (*derived*) |
| You owe / negative / error | `#B0391F` (brick, *derived* from the clay/espresso family, about 5.7:1) | `#FF8C73` (*derived*) |

Always pair with a sign or arrow (+/−, ↑/↓) so green vs. red is never the only signal.

### Usage rules (acceptance checks for every screen)
1. Emerald covers no more than about 10% of the screen. Normally **one** Emerald element per screen: the primary action (FAB or main button), plus at most one highlight (e.g., the active budget segment).
2. The Espresso/Sand/Clay family carries structure: hero summary card (Espresso), chips and containers (Sand), icons and mid-tone fills (Clay).
3. Everything else is Mist/White. When in doubt, use neutral.
4. **Never show balance amounts or semantic green/red on Sand** (contrast fails). Put amounts on Mist, White or Espresso.
5. Emerald fills are for actions and highlights only, never for "positive balance" fills, so action and "owed to you" are never confused. Positive balances use the darker `#007A55` text with a + sign.
6. Charts use a sequential ramp: Sand `#E0D0C1` → Blush `#C5A092` (*derived*) → Clay `#A76D60` → Rust `#85402C` (*derived*) → Espresso `#601700`. The highlighted slice/bar is Emerald. Category icons are monochrome Clay or Espresso on a Sand tint. No rainbow category colors.
7. Verify every text/background pair with a contrast checker (4.5:1 body, 3:1 large text and icons) before release.

### Component color mapping
- **Hero card (Home, Group header):** Espresso background, Mist text, amount highlights in `#3DDBA8` or Mist, budget bar track in Clay at 40%, fill in Emerald.
- **Chips:** Sand background, Espresso text. Selected: Espresso fill, Mist text.
- **Bottom navigation:** Mist surface with a Sand pill behind the selected item, Espresso icon/label. Unselected in Clay-dark `#7A5A50`.
- **FAB and primary button:** Emerald with `#1A0900` icon/label.
- **Budget progress:** Emerald, then Clay at 80%, then Brick `#B0391F` at 100%.
- **Dividers/outlines:** Sand at 60%.

## 3. Typography (distinctive, free, bundled)

All fonts below are open-licence families from Google Fonts (SIL OFL). Record them in `docs/ASSETS.md`.

| Role | Font | Why |
|---|---|---|
| **Display, headings, hero amounts** | **Bricolage Grotesque** (variable, weights 600-800) | Characterful grotesque with quirky details. It gives the app a recognisable personality instead of the default Material look |
| **Body and UI** | **Figtree** | Clean, friendly, highly legible next to the display face |
| **Ledger numbers (lists, numpad, balances, tables)** | **DM Mono** (Medium) | Monospaced digits align perfectly in columns and give a ledger/receipt feel |

Alternates if a font fails the checks below: Syne or Unbounded (display), Plus Jakarta Sans (body), JetBrains Mono (numbers).

| Style | Font / size / weight | Use |
|---|---|---|
| Display amount | Bricolage Grotesque 44sp / 700 | Hero totals, numpad amount |
| Title large | Bricolage Grotesque 24sp / 700 | Screen titles |
| Title medium | Bricolage Grotesque 18sp / 600 | Card titles, section headers |
| Body | Figtree 14-16sp / 400-500 | Content |
| Label | Figtree 12sp / 600 | Chips, captions |
| Amount in rows | DM Mono 16sp / 500 | Lists, balances, split previews |

### Font checks the agent must complete (known risk areas)
1. **Rupee sign (₹, U+20B9):** confirm each font actually has the glyph. If one lacks it, Android falls back to a system font, which can look mismatched. Add a visual test screen. If it mismatches, render ₹ as a separate `Text` with a bundled font that includes it (a tiny Noto Sans subset), aligned on the baseline.
2. **Hero count-up jitter:** Bricolage digits are proportional, so a counting number would change width. Use the odometer-style animated counter described in §6 with each digit in a fixed-width slot.
3. **Bundle size:** subset fonts to Latin plus `₹` and common punctuation. Variable fonts are fine if the total stays small.
4. **Future Hindi:** keep Noto Sans Devanagari as the fallback family when localization starts.
5. Check rendering at 200% font scale.

## 4. Layout, shape, elevation

- 8dp grid. Screen padding 16dp. Card padding 16dp. Section gap 24dp.
- Corner radius: cards 24dp, chips 14dp, sheets 28dp (top), buttons fully rounded.
- Elevation: tonal color and a faint Sand outline instead of heavy shadows. A soft warm shadow (Espresso at 8% alpha) for floating elements only (FAB, snackbar).
- Min touch target 48dp. Primary actions in the thumb zone (bottom).
- Signature detail: the hero card has a subtle "receipt edge" (zig-zag or scalloped bottom edge) in Espresso, echoing a ledger slip. Use it on the hero card and shareable summary images, nowhere else.

## 5. Navigation and screens

Bottom navigation (4 tabs) with a centered Emerald FAB for quick add:
**Home · Groups · [+] · People · Insights**. Settings via the avatar icon on Home.

| Screen | Contents |
|---|---|
| **Onboarding** | 3 short slides (private/offline, personal + group, honest spending), then your name, optional monthly budget, optional opening balances |
| **Home (My Money)** | Espresso hero card: month spent (odometer count-up), budget bar, safe-to-spend today. Below: category breakdown, recent transactions, receivable/payable strip ("Owed to you ₹1,680 · You owe ₹200") |
| **Quick add (sheet)** | Large amount in the display face, DM Mono numpad, category chips, account chip, date chip, note. Save button is Emerald |
| **Groups list** | Cards with group name, member avatars, your net with +/− and arrow, settled badge |
| **Group detail** | Espresso header with net summary. Tabs: **Balances** (nets, simplified/original toggle), **Expenses** (list with edit trail), **Summary** (shareable preview). FAB adds an expense |
| **Add group expense** | Amount, description, payer chip(s), split selector (Equal · Exact · % · Shares · Itemized) with live per-person preview and a "left to assign" indicator |
| **Settle up** | Suggested payments as cards ("Pay Rahul ₹840"), Record payment sheet, celebration at zero |
| **People** | Net per person across ledger and groups; Person detail with timeline and Settle/Lend/Borrow actions |
| **Insights** | Month vs last month, top categories, small-payment insight, trend line |
| **Settings** | Sounds, haptics, reduce motion, theme, app lock, categories, accounts, export/import, about |
| **Share preview** | Receipt-style image/text of the summary and the Android share action |

Empty states: simple warm vector illustration in the palette, one sentence, one action.

## 6. Components

- **Amount text:** sign-aware, DM Mono, semantic colors with +/− prefix for balances.
- **Odometer counter:** each digit sits in a fixed-width slot and rolls vertically to its new value (about 400 ms, staggered by 30 ms per digit). Separators and ₹ stay static.
- **Net badge:** pill with arrow icon, "Owed ₹400" / "Owes ₹840", on Mist/White only.
- **Person avatar:** initials on a Sand tint, with deterministic variation between Sand, Blush and Clay-tint by ID.
- **Category chip:** icon + label, selected state Espresso fill.
- **Split preview row:** avatar, name, amount, editable field depending on split type.
- **History row:** description, who/when, amount, "edited" tag that opens the edit trail.
- **Snackbar with Undo** for deletes.
- **Progress bar** for budgets (colors in §2).
- **App icon:** adaptive icon with Espresso background and a Mist ledger/coin mark with a small Emerald dot. Monochrome layer provided for themed icons.

## 7. Motion

Principles: motion explains change and rewards completion. It is quick, never blocks input, and is skipped when reduced motion is on.

| Token | Duration | Easing |
|---|---|---|
| `fast` | 120 ms | EaseOut |
| `standard` | 250 ms | EmphasizedDecelerate |
| `emphasized` | 400 ms | EmphasizedDecelerate |
| `spring` | spring(dampingRatio 0.75, stiffness Medium) | FAB and chip selection |

Animations to implement:
- **Odometer count-up** for hero totals and balances.
- **Coin drop** on saving an expense: a small coin drops into the list row, and the new row slides in with `standard`.
- **FAB:** press scale to 0.92 with spring release; expands into the quick-add sheet via a container transform.
- **Chip selection:** spring scale pulse with color cross-fade (Sand → Espresso).
- **Balance change:** amounts animate to new values; a brief Sand highlight flash on the changed row.
- **Swipe to delete:** row follows the finger, collapses with `standard`, then the Undo snackbar.
- **Settle-up completion:** net badge morphs from amount to a checkmark (Lottie or Canvas path).
- **All square celebration:** confetti burst (Lottie, ≤ 1.2 s, ≤ 50 KB) in palette colors only, plus a headline "All square 🎉".
- **Screen transitions:** container transform for list → detail; fade-through between tabs.
- **Receipt edge reveal:** the share preview "prints" downward (short slide-reveal, 400 ms).

## 8. Reaction moments

| Moment | Visual | Sound | Haptic |
|---|---|---|---|
| Expense saved | Coin drop, row slides in | `coin_tick` | Light confirm |
| Group expense saved | Avatars pulse with new shares | `coin_tick` | Light confirm |
| Settlement recorded | Check morph, balance counts down | `settle_chime` | Confirm |
| Group reaches zero | Confetti, "All square 🎉" | `success_flourish` | Strong confirm |
| Budget reaches 80% | Clay nudge card slides in ("You're at 80%, ₹210/day left") | none | Light tick |
| Budget exceeded | Progress bar gives one gentle shake, friendly message, no scolding | `soft_down` (quiet) | Light reject |
| Small-payments insight | Playful insight card ("₹1,240 in tiny UPI payments 🐀💸") | none | none |
| Logging streak (3, 7, 30 days) | Small flame badge pops | `pop` | Light tick |
| Delete | Row collapses, Undo snackbar | `whoosh` (short) | Light |
| Share summary | Receipt image slides to the share sheet | `whoosh` | none |
| Input error | Field shakes (2 cycles, 8dp) | none | Reject |

Tone of copy: warm, brief, never shaming about spending.

## 9. Sound design

- Sounds are short (≤ 300 ms, `success_flourish` ≤ 800 ms), soft, mixed low (peak around −12 dBFS). OGG Vorbis, mono, ≤ 30 KB each.
- Files in `res/raw`: `coin_tick.ogg`, `pop.ogg`, `whoosh.ogg`, `settle_chime.ogg`, `success_flourish.ogg`, `soft_down.ogg`. Character: warm and wooden/coin-like to match the palette, not sharp or electronic.
- Source from **CC0 / properly licensed** libraries (or generate originals) and record each licence in `docs/ASSETS.md`.
- Playback via `SoundPool` with `USAGE_ASSISTANCE_SONIFICATION`. Preload on a background thread.
- No sound when the ringer is silent or vibrate. Never grab audio focus.
- Settings: **Sounds** on/off (default on). Sound is never the only feedback for anything.

## 10. Haptics

Use `HapticFeedbackConstants` (CONFIRM, REJECT, CLOCK_TICK, LONG_PRESS). Settings: **Haptics** on/off (default on). Follow the system touch-feedback setting.

## 11. Accessibility

- Reduce motion: skip or shorten animations when the in-app switch is on or `ANIMATOR_DURATION_SCALE` is 0 (the odometer becomes an instant change).
- TalkBack reads every amount in full ("Rahul owes you 840 rupees"). Custom content descriptions on icons, badges and chart elements.
- Charts have a text/table alternative.
- Font scale supported to 200% without clipping.
- Color is never the sole signal for positive/negative.

## 12. Theming implementation notes

- Define tokens in one place (`:core-ui`): `LedgrColors`, `LedgrTypography`, `LedgrShapes`, `LedgrMotion`, `LedgrSounds`.
- Map to Material 3 `ColorScheme`: `primary` = Emerald (`onPrimary` = `#1A0900`), `secondary` = Espresso (`onSecondary` = Mist), `tertiary` = Clay, `background` = Mist, `surface` = White, `secondaryContainer` = Sand, `onSurface` = Espresso, `error` = Brick. Expose `positive`, `negative`, `positiveOnDark`, `negativeOnDark`, `heroSurface` and chart ramp via a custom `CompositionLocal`.
- Provide `@Preview`s for every component in light/dark and at large font scale.
- No hard-coded colors, fonts or dp values outside `:core-ui`.
- Add a "design QA" debug screen that shows the palette, type scale, ₹ glyph test, and every semantic color on its allowed backgrounds.
