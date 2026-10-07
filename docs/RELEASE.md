# RELEASE — publishing Ledgr

Do this only after the validation step (`VALIDATION_KIT.md`) says the app is worth releasing. Policies change, so re-check the Play Console's current requirements before each step. Nothing here is legal advice.

## 1. Choose how to distribute

| Route | Effort | Notes |
|---|---|---|
| **Side-load a signed APK** to friends | Lowest | Good for early testing. Users must allow installs from unknown sources. No store review |
| **GitHub Releases** (APK download) | Low | Fine for a portfolio project; counts as a public release of your work |
| **Google Play** | Highest | Reach and trust, but there is a registration fee and a testing requirement (below) |

**Google Play facts to plan around (verified Oct 2026, re-check before you start):**
- One-time developer registration fee of US$25 and identity verification.
- A **personal** account created after 13 Nov 2023 must run a **closed test with at least 12 testers opted in for 14 continuous days** before it can apply for production access. **Organization** accounts are exempt. The requirement applies to each new app on a personal account.
- Plan roughly three weeks from first upload to production. Recruit your 12 testers early. Friends and classmates from the validation interviews are the natural group, and they must stay opted in for the whole period.
- Google requires new apps to target a recent Android API level, and the bar rises every year. Check the current minimum in the Play Console before building the release.

## 2. Pre-release engineering checklist

- [ ] Version name `1.0.0`, `versionCode` increments on every upload
- [ ] Release build uses R8 minification and resource shrinking, and tests pass on the release variant
- [ ] Merged release manifest has **no INTERNET permission** (CI check from `TECH_STACK.md`)
- [ ] `allowBackup=false`; no analytics or crash SDKs that transmit data
- [ ] App lock works on the physical phone; fallback to device credential works
- [ ] Export → import round trip verified on the release build
- [ ] Goa-trip golden scenario run end to end on the physical phone
- [ ] Font and sound licences recorded in `ASSETS.md`; Licenses screen shows them
- [ ] Tested at 200% font scale, dark mode, TalkBack basic pass, reduced motion on
- [ ] Tested on at least two Android versions (e.g., API 26-28 and the latest) and one low-end device if possible
- [ ] Upgrade test: install previous version, add data, install new version, data and balances intact
- [ ] Adaptive + monochrome app icon, splash screen, Play Store graphics ready

## 3. Signing

1. Use **Play App Signing**. You keep an **upload key**, and Google holds the app signing key.
2. Generate the upload keystore once. Back it up in **two separate places** (e.g., encrypted drive + password manager). Losing it makes updates painful.
3. Never commit the keystore or passwords. Use `local.properties` or environment variables, and add them to `.gitignore`.
4. Build an **Android App Bundle** (`.aab`) for Play. For side-loading or GitHub Releases, build a signed APK.

## 4. Store listing draft

*Replace the working title once you pick a name and check availability.*

**App name (≤ 30 chars):** Ledgr: Expenses & Splits
**Short description (≤ 80 chars):** Private, offline expense tracker with group splits. No account needed.

**Full description (≤ 4000 chars):**
> Ledgr keeps track of your money and the money you share with friends, all on your phone. No account. No internet. Nothing leaves your device unless you choose to share it.
>
> **Your money**
> • Add expenses in a few taps with a fast numpad
> • Categories, cash/UPI/card accounts, monthly budget and a "safe to spend today" number
> • See where your money actually went, not just what you paid
>
> **Shared money**
> • Create groups for trips, flats and nights out
> • Split equally, by exact amounts, percentages, shares or itemized bills with tax and tip
> • Handles multiple payers, partial payments and refunds
> • Clear "who pays whom" with simplified payments, or see the original flow
> • Friends don't need the app: share a clean summary on WhatsApp or anywhere else
>
> **The difference**
> When you pay for a group dinner, your personal spending shows only **your share**, and the rest is tracked as money owed to you. Your numbers stay honest.
>
> **Private by design**
> • Works fully offline
> • Optional fingerprint/face lock
> • Backup and restore with a file you control
>
> Made for students, flatmates and friends who share costs.

**Category:** Finance (or Productivity). **Tags/keywords:** expense tracker, split bills, budget, offline.
**Screenshots (6-8):** Home, quick add, group balances, itemized split, settle up/All square, share summary, dark mode. Use the palette and add short captions.

## 5. Play Console forms (answers to expect)

- **Data safety:** the app does not collect or share user data; all data stays on the device. Data the user explicitly exports or shares via the system share sheet is user-initiated, not collected by you. Read the form's current wording carefully and answer accurately.
- **Permissions declared:** vibrate, biometric. Nothing sensitive.
- **Content rating questionnaire:** no violence, no user-generated public content, no gambling. Expect an "Everyone" rating.
- **Target audience:** 18+ or "not designed for children". Money apps are best declared as not child-directed.
- **Ads:** none.
- **Financial features declaration:** declare it is a personal finance **tracker** and that it does **not** provide loans, payments or money transfers (it never moves money).
- **Privacy policy URL:** required. Host the text below on a public page (GitHub Pages works).

## 6. Privacy policy draft (host publicly; fill the placeholders)

> **Privacy Policy for Ledgr**
> Last updated: {date}
>
> **Summary:** Ledgr stores all of your data on your own device. We do not collect, transmit or sell your data, and the app does not have internet access permission.
>
> **What the app stores:** the expenses, groups, people names, accounts and settings you enter. This data stays in the app's private storage on your device.
>
> **What we collect:** nothing. There are no accounts, analytics, advertising or crash-reporting services in the app.
>
> **Sharing:** the app only shares data when you choose to. If you export a backup file or share a group summary, the file or text goes where you send it (for example a chat app) under that service's own policies. Names you enter for other people are stored only on your device. Please share summaries only with people in the group.
>
> **Permissions:** Biometric (to unlock the app if you turn on app lock) and Vibrate (for haptic feedback). The app does not request internet, contacts, location, storage or SMS access.
>
> **Backups:** Android cloud backup is turned off for this app. You can create and restore backups yourself via the Export/Import feature. Backup files are not encrypted, so store them securely.
>
> **Deleting data:** uninstalling the app or clearing its storage permanently deletes all data on the device.
>
> **Children:** the app is not directed at children under 13.
>
> **Changes:** if this policy changes, the date above will be updated.
>
> **Contact:** {your email}

## 7. Release steps (Play)

1. Create the developer account (personal or organization) and finish verification.
2. Create the app, complete the store listing, data safety, content rating and financial declarations.
3. Upload the `.aab` to **Internal testing** first (fast, no review) and fix any issues.
4. If your account needs it, create a **Closed testing** track, add at least 12 testers by email, and keep them opted in for 14 continuous days. Collect feedback and ship fixes as new uploads without pausing the track.
5. Apply for production access in the Console, then roll out to production, starting with a staged percentage if offered.
6. Monitor Play Console's Android vitals (crashes, ANRs) and respond to reviews.

## 8. After launch
- Bug-fix updates first. Keep a changelog.
- Remind users inside the app to export backups (already in the UX spec).
- Track requests in a simple list. Don't add telemetry. Ask users directly (e.g., a link to a feedback form in Settings → About).
- Revisit `VALIDATION_KIT.md` Part C with real users after two weeks.
