# ML_PLAN — on-device models for Ledgr (v1.5 and v2)

Not part of v1. This document defines the work so the app is ready for it (the `:ml` seam exists from M0) and so the project doubles as a **research artifact** for PhD applications.

All inference runs **fully on the phone**. No cloud calls at runtime. Training happens in a separate Python repository (`ledgr-ml`).

## 1. Tasks

| ID | Task | Version | Input → Output |
|---|---|---|---|
| **T1 Categorizer** | Suggest a category for an expense | v1.5 | description text (+ optional amount, time of day) → ranked categories with scores |
| **T2 Parser** | Turn a free-text message (English, Hindi, Hinglish) into a structured entry | v2 | "kal Rahul ke saath 450 ka dinner kiya, maine pay kiya" → structured JSON |

The app keeps decisions deterministic: models only **suggest**. The user confirms before anything is written. Name resolution ("Rahul" → which Rahul) and all money math stay in app code and `:engine`.

## 2. T1 — Categorizer

**Goal:** top-1 accuracy high enough that the suggested chip is right most of the time, and top-3 almost always.

**Data**
- Seed: synthetic descriptions per category (templates + vocabulary lists such as "chai", "samosa", "recharge", "mess fee", "auto", "petrol", "movie ticket"), including typos and mixed-script/Hinglish variants.
- Real: your own and consenting friends' expense descriptions, labeled with the app's categories. Even 500-1,000 real examples make a big difference.
- **Personalization (on-device):** learn from the user's own confirmed categories (see §6).

**Models to compare (start simple)**
1. Keyword/rule baseline
2. Character n-gram TF-IDF + logistic regression (tiny, fast, handles typos)
3. Small embedding model + linear head
4. A small distilled/multilingual transformer fine-tuned for classification (only if 2-3 fall short)

Pick the **smallest model that meets the targets** in §5. A 1-5 MB model is ideal.

## 3. T2 — Parser

**Output schema (matches the app's entry model; amounts in paise):**
```json
{
  "intent": "expense | lend | borrow | settlement | unknown",
  "amount_paise": 45000,
  "description": "dinner",
  "category_hint": "Meals",
  "date_hint": "yesterday",
  "payer_mention": "me | <name> | multiple | unknown",
  "participant_mentions": ["Rahul"],
  "split_hint": "equal | exact | percent | shares | unknown",
  "split_details": [{"mention": "Aman", "amount_paise": 20000}],
  "counterparty_mention": null,
  "confidence": 0.0
}
```
Mentions are raw strings. The app resolves them to people. Dates are hints ("yesterday", "kal") resolved by app code. If `confidence` is low or `intent` is unknown, the app opens the manual form pre-filled with what was understood.

**Example utterances (include many like these in training and test data)**
- "I paid 450 for dinner for me, Rahul and Aman"
- "Rahul paid 1200 for the hotel"
- "chai 30"
- "kal Rahul ke saath 450 ka dinner kiya, maine pay kiya"
- "Aman ne 1200 hotel ke diye, hum 4 log the"
- "Rohit ko 500 udhaar diye"
- "Aman ne 250 wapas diye"
- "petrol 3000 maine bhara, Rahul Aman Rohit ke saath equal"

**Approach options**
1. Rules + regex baseline (amount and simple patterns)
2. **Token tagging** (amount, item, person mentions, payer cues, split cues) with a small encoder, then rule-based assembly into the schema
3. Small seq2seq that outputs the JSON directly (heavier; only if tagging can't reach the targets)

Recommended path: 2, because it is small, fast, and each field can be evaluated separately. Evaluate 3 as an ablation if time allows.

## 4. Data plan

**Collection and privacy**
- Collect messages only from consenting people. Strip real names (replace with a name list) before storing. Keep raw data off GitHub. Keep only the anonymized versions in the repo.
- Record the collection process, how many contributors, and the languages and mix (English / Hindi in Roman script / Devanagari / Hinglish) for the write-up.

**Synthetic generation**
- Build templates per intent with slots (amount formats like "450", "4.5k", "₹450", "450 rs", people, items, split cues, Hindi/English connectors).
- Paraphrase with spelling variation and code-switching. If you use an LLM to help generate synthetic data, treat it as a **training-data tool only**, document it, and never ship it in the app.
- Keep synthetic and real data strictly separated in splits.

**Splits**
- Train: synthetic plus real train portion
- Validation: a mix, used for tuning
- **Test: real, hand-labeled, frozen** before any model tuning (200-500 utterances). Never train or tune on it. Report all results on it
- Also create a small **adversarial set**: ambiguous names, missing amounts, numbers that are not amounts ("Room 204"), negations, multiple expenses in one message

**Labeling guide (write before labeling)**
- One message = one entry in v2. Multiple expenses in one message are marked `unknown` and handled by asking the user to split them.
- When the payer isn't stated, label `unknown` (the app defaults to "me").
- Amount normalization rules for "k", "hazar", "sau", "lakh".
- Two labelers on 20% of data to measure agreement.

## 5. Metrics and budgets

**T1:** top-1 and top-3 accuracy, macro-F1 (rare categories matter), calibration, per-category confusion matrix.
**T2:** exact-match on the whole structure; per-field accuracy/F1 (intent, amount, payer, participants, split type); confidence calibration (does low confidence predict errors?); **selective accuracy** (accuracy on the portion of messages the model answers when it abstains below a threshold).

**Baselines to report:** rules/regex; a large general-purpose language model few-shot, **used only as an offline upper-bound reference in the paper, never shipped**.

**On-device budgets (targets to tune):** model ≤ 10 MB (T1 ≤ 5 MB), inference < 50 ms on a mid-range phone for T1 and < 150 ms for T2, no network, memory under about 100 MB while running.

## 6. On-device personalization (T1, optional stretch)

- Keep a small local table of (normalized description → category) the user has confirmed. Check it **before** the model (exact/fuzzy match).
- Optionally fine-tune only a linear head on-device from confirmed entries. Everything stays on the device. This is a nice contribution to discuss: personalization without any server.

## 7. Export and integration

- Train in Python (PyTorch or TensorFlow/Keras), export to **TFLite / LiteRT** (or ONNX if that's easier), quantize (int8 or float16) and re-measure accuracy after quantization.
- Ship as an asset in `:ml` behind the existing interfaces:
```kotlin
interface Categorizer { fun suggest(description: String): List<CategorySuggestion> }
interface ExpenseParser { fun parse(text: String): ParsedEntry? }
```
- Add a golden test file of utterances → expected parsed output so regressions are caught when the model is replaced.
- UX: suggestions appear as a highlighted chip (T1) or a pre-filled form with an "Edit" focus on uncertain fields (T2). Never auto-save.

## 8. Evaluation write-up (the research artifact)

Structure it like a short paper or technical report:
1. Problem and why on-device (privacy, offline, latency)
2. Dataset: collection, size, language mix, annotation guide, agreement
3. Models and baselines, training details
4. Results on the frozen real test set, error analysis, adversarial set
5. Ablations: synthetic-only vs synthetic+real, effect of data size, model size vs accuracy, quantization effect, with/without personalization
6. Limitations and ethics (data consent, language coverage, bias towards your friends' speech)
7. Reproducibility: code, seeds, scripts, and a data card

Publish the anonymized dataset only if consent covers it. Otherwise release the generation scripts and the evaluation protocol.

## 9. Suggested timeline

| When | Work |
|---|---|
| During v1 build | Collect and label real descriptions in the background. Define the labeling guide |
| v1.5 | T1: baselines → best small model → integrate suggestions chip → measure on own usage |
| v2 | T2: dataset, tagging model, assembly rules, confirmation UX, evaluation report |
| Whenever | Write up while results are fresh |

## 10. Risks
- **Language mix is narrow:** your data reflects your circle's speech. State this plainly in the write-up.
- **Small real dataset:** keep the model small and regularized; report confidence intervals.
- **Model suggests, never decides:** a wrong amount is a trust-breaking bug, so the amount is always shown for confirmation.
- **Scope creep:** do T1 fully before starting T2.
