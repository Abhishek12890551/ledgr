# ASSETS — fonts, sounds, animations, icons

Every third-party asset must be listed here **before it is committed**, with its source and licence. If the licence is unclear, don't use the asset. The About screen's "Licenses" page is generated from this file plus Gradle dependency licences.

## 1. Fonts (bundle in `res/font`, subset to Latin + ₹ + common punctuation)

| Font | Role | Licence | Where to get it | Status |
|---|---|---|---|---|
| Bricolage Grotesque (variable) | Display, headings, hero amounts | SIL Open Font License 1.1 | Google Fonts | TODO download |
| Figtree | Body and UI | SIL Open Font License 1.1 | Google Fonts | TODO download |
| DM Mono (Medium) | Ledger numbers | SIL Open Font License 1.1 | Google Fonts | TODO download |
| Noto Sans (₹ fallback subset, only if needed) | ₹ glyph fallback | SIL Open Font License 1.1 | Google Fonts | Only if the ₹ test fails |
| Noto Sans Devanagari | Future Hindi fallback | SIL Open Font License 1.1 | Google Fonts | Later |

OFL allows bundling in apps. Keep each font's licence text in `third_party/licenses/`.
**Test:** the ₹ glyph in all three main fonts (see `DESIGN_SYSTEM.md` §3).

## 2. Sounds (`res/raw`, OGG Vorbis, mono, ≤ 30 KB, peak about −12 dBFS)

Character: warm, soft, coin/wood/glass. Never sharp or electronic.

| File | Used for | Length | Character | Source | Licence | Status |
|---|---|---|---|---|---|---|
| `coin_tick.ogg` | Expense saved | ≤ 200 ms | Light coin tap | TODO | TODO | TODO |
| `pop.ogg` | Chip select, streak badge | ≤ 150 ms | Soft bubble pop | TODO | TODO | TODO |
| `whoosh.ogg` | Delete, share | ≤ 250 ms | Short airy swish | TODO | TODO | TODO |
| `settle_chime.ogg` | Settlement recorded | ≤ 400 ms | Two-note soft chime | TODO | TODO | TODO |
| `success_flourish.ogg` | All square | ≤ 800 ms | Gentle rising arpeggio | TODO | TODO | TODO |
| `soft_down.ogg` | Over budget | ≤ 300 ms | Quiet low descending tone, not scolding | TODO | TODO | TODO |

**Sourcing options (check the licence of each individual file):**
- CC0 / public-domain libraries, e.g. the CC0 filter on Freesound, or Kenney's CC0 audio packs
- Generate originals yourself (a simple synth or a free DAW) and license them under your own terms
- Avoid anything marked "attribution required" unless you add the credit here and on the About screen. Avoid "non-commercial" licences if you might ever publish the app

**Processing:** trim silence, normalize, mono, convert to OGG, check the file size, and A/B on the physical phone speaker at low volume.

## 3. Animations (Lottie JSON in `res/raw`, palette colors only)

| File | Used for | Max size | Source | Licence | Status |
|---|---|---|---|---|---|
| `confetti.json` | All square | 50 KB, ≤ 1.2 s | TODO | TODO | TODO |
| `check_morph.json` | Settlement completion | 20 KB | TODO | TODO | TODO |
| `coin_drop.json` (or Canvas-drawn) | Expense saved | 20 KB | TODO | TODO | TODO |
| Illustrations (empty states, 4) | Home, Groups, Group expenses, People | SVG/vector drawable, ≤ 10 KB each | Draw your own or CC0 | TODO | TODO |

Lottie files from community sites have per-file licences. Check each one, and recolor to the palette (Emerald, Mist, Sand, Clay, Espresso). Drawing simple ones yourself in a vector tool avoids any licensing question.

## 4. Icons
- **Material Symbols** (Rounded, weight 400). Apache 2.0. Use the Compose Material icons library or export only the glyphs needed.
- App icon: original artwork (Espresso background, Mist ledger/coin mark, small Emerald dot). Provide adaptive and monochrome layers.

## 5. Libraries
Gradle dependency licences are collected with a licence-report plugin or by hand into `third_party/licenses/`. Allowed: Apache 2.0, MIT, BSD, OFL. Not allowed without discussion: GPL/AGPL.

## 6. Rules
1. No asset without a row in this file.
2. Record the exact download URL or the tool used to generate it.
3. Keep original, unprocessed files out of the repo unless small. Keep processed files and a note of what was changed.
4. Re-verify the licence if a file is replaced.
