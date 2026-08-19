# APG-2580 – Delivery Tracker

**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Planning branch:** `APG-2580/planning-docs`
**Planning agent (this file's owner):** Copilot in this IDE session
**Last updated:** 2026-08-19

This document is the live single source of truth while APG-2580 is being delivered across four dependent implementation branches. The planning agent (Copilot in this workspace) owns and updates this file. Each implementation branch is worked by a fresh implementer agent (in a separate chat/session or by a human) which is given the branch-specific prompt copied from this document.

---

## Workflow

For each branch, the loop is:

1. **Planning agent → user:** Hands the user the *Implementer prompt* for the next branch (from this file).
2. **User → implementer agent:** Pastes the prompt into a fresh agent session (new Copilot chat / different tool).
3. **Implementer agent → user:** Reports back with (a) the diff/PR link, (b) test output, (c) any deviations from the plan, (d) any questions.
4. **User → planning agent:** Pastes the implementer's report here.
5. **Planning agent:** Verifies the report against the original plan, updates this tracker (status, notes, follow-ups), regenerates the *next* implementer prompt with any deltas learnt, and hands it back to the user.

Nothing progresses to the next branch until the current branch is marked ✅ in this tracker.

---

## Reference documents (do NOT duplicate content — link only)

| Doc | Purpose |
|-----|---------|
| [`README.md`](./README.md) | Requirement-to-branch traceability matrix, conventions, downstream-impact analysis |
| [`APG-2580-branch-1-remove-pii-and-duplicate-sections.md`](./APG-2580-branch-1-remove-pii-and-duplicate-sections.md) | Branch 1 – exact code changes |
| [`APG-2580-branch-2-attendance-codes-to-descriptions.md`](./APG-2580-branch-2-attendance-codes-to-descriptions.md) | Branch 2 – exact code changes |
| [`APG-2580-branch-3-enum-display-names.md`](./APG-2580-branch-3-enum-display-names.md) | Branch 3 – exact code changes |
| [`APG-2580-branch-4-attendance-session-note-restructure.md`](./APG-2580-branch-4-attendance-session-note-restructure.md) | Branch 4 – exact code changes |
| [`APG-2580-cleanup-sar-factories-plan.md`](./APG-2580-cleanup-sar-factories-plan.md) | Sweep-up (post-PR#860) – exact code changes to remove orphaned SAR test factories and fix cohort assertion |
| [`../how-to/update-sar-tests.md`](../how-to/update-sar-tests.md) | SAR fixture regeneration workflow (used at the end of every branch) |
| `scripts/local-scripts/regenerate-sar-snapshots.sh` | One-shot fixture regenerator; preferred over manual copy steps in the branch docs |

---

## Corrections / clarifications discovered during validation

These override the corresponding paragraphs in the branch docs. The planning agent has verified each against the current source on `main` (commit at time of writing: `00dc2ebe`).

1. **Fixture regeneration** — Every branch doc describes a two-step process (`SAR_GENERATE_ACTUAL=true ./gradlew …` then `cp <path>/*.log …`). The repo already ships `scripts/local-scripts/regenerate-sar-snapshots.sh` that does both steps and cleans up the `.log` files. **Use the script in every branch.**
2. **`SlotName` location (Branch 3)** — The enum is defined inline at the bottom of `src/main/kotlin/.../entity/AvailabilitySlotEntity.kt` (line 38), not in a standalone `entity/type/SlotName.kt` file. Values are `DAYTIME(10, "daytime")` and `EVENING(20, "evening")` — lowercase display names, hence the `replaceFirstChar { it.uppercase() }` in the mapper.
3. **`ATTC` description example (Branch 2 vs README)** — README asserts the factory writes `"Attended - Complied"`; Branch 2 doc text says `"Attended"`. Whatever the seed data / factory currently writes will end up in the regenerated fixture — no code change needed either way. Treat any human-readable text as acceptable; do not manually edit fixture text.
4. **`SubjectAccessRequestServiceTest.kt` line numbers (Branch 1)** — Verified exact. File currently has 316 lines. All line numbers cited in Branch 1 doc match the checked-out file.
5. **Downstream impact** — Confirmed via grep. No files outside those listed in Branch docs reference: `SubjectAccessRequestGroupWaitlistItemView`, `SubjectAccessRequestReferralCaseListItemView`, `outcomeTypeCode`. ~~Factories (`GroupWaitlistItemViewEntityFactory`, `ReferralCaseListItemViewEntityFactory`) stay in place — used by other non-SAR tests.~~ **[Superseded]** Re-verified after PR #860 — both factories have zero external call-sites and are fully orphaned. They are cleaned up in the follow-up sweep-up branch `APG-2580/cleanup-orphaned-sar-factories` (plan doc committed on that branch); execute after PR #860 merges to `main`. See correction #7.
6. **`HmppsSubjectAccessRequestControllerIntegrationTest` (Branch 1 discovery)** — Branch 1 doc §6 claimed no changes were needed to this file, but the test `should return 200 on GET subject access request data` asserted `content.containsKey("groupWaitlistItemViews")` / `containsKey("referralCaseListItemViews")` and had to lose those 4 lines. Branches 2–4 do NOT change the top-level shape of `SubjectAccessRequestContent`, so this file is not expected to break again. But whenever any branch renames or removes a field on any SAR DTO, grep `HmppsSubjectAccessRequestControllerIntegrationTest.kt` for that field name to be safe. Added to every future implementer prompt as a mandatory pre-flight check.
7. **Orphaned SAR test factories & pre-existing cohort assertion warning (9-eyes review discoveries)** — Post-Branch 1 grep confirmed `GroupWaitlistItemViewEntityFactory` and `ReferralCaseListItemViewEntityFactory` have zero call-sites (contradicting the original wording of clarification #5). Additionally, `SubjectAccessRequestServiceTest.kt` line 125 compares `List<String?>` (DTO `.cohort`) to `List<OffenceCohort>` (entity `.cohort`) — silently always-false for non-empty sets. Both fixed on branch `APG-2580/cleanup-orphaned-sar-factories`; plan doc [`APG-2580-cleanup-sar-factories-plan.md`](./APG-2580-cleanup-sar-factories-plan.md) (also committed on the sweep-up branch). To be executed after PR #860 merges to `main` — see the [sweep-up implementer prompt](#implementer-prompt--sweep-up-post-pr-860-delete-orphaned-sar-factories--fix-cohort-assertion) below. Not blocking for Branches 2–4.
8. **`referralCohortHistories` flaky ordering (Branch 1 post-main-merge discovery)** — After merging `main` (which brought Gradle deps bump #848 including Hibernate/byte-buddy version changes), the SAR JSON contract test failed because `ReferralEntity.referralCohortHistories` is a `MutableSet` annotated with only `@OrderBy("createdAt DESC")`, and two seeded rows share the same `createdAt` value. Tie-breaking is Hibernate-version-dependent → non-deterministic. **Fix applied on Branch 1 as commit `bab0cb03`:** deterministic secondary sort by `id` in `SubjectAccessRequestReferral.toApi()` (5 lines: 1 import + a `.sortedWith(compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }.thenBy { it.id })` before the existing `.map { it.toApi() }.toMutableSet()`). Zero fixture change — locks in the historically-observed order. **Pattern for Branches 2–4:** any `MutableSet<...>` field on a SAR DTO that is later JSON-serialised is at risk of the same flake unless the source has total ordering. `programmeGroupMemberships`, `referralLdcHistories` and the Branch 4 restructured attendance/note collections all fall in this category. If a future branch adds or reshapes any collection field on a SAR DTO, either source it from an already-sorted `List` or apply an explicit `.sortedWith(...)` in the mapper before `.toMutableSet()`.

---

## Status board

| Branch | Doc | Status | PR | Merged | Notes |
|--------|-----|--------|----|--------|-------|
| 1. `APG-2580/remove-pii-and-duplicate-sections` | [Branch 1](./APG-2580-branch-1-remove-pii-and-duplicate-sections.md) | 🔎 Awaiting human review | [#860](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api/pull/860) | — | Commits `679c35bd` + main merge `80ea504a` + cohort-ordering fix `bab0cb03`. All tests pass locally after fix. Clarification #7 added. |
| 1.5 sweep-up `APG-2580/cleanup-orphaned-sar-factories` | [Sweep-up plan](./APG-2580-cleanup-sar-factories-plan.md) | 🛑 Blocked on PR #860 merging to `main` | — | — | Small tidy-up PR from the 9-eyes review of PR #860. Deletes 2 now-orphaned test factories and fixes 1 pre-existing `List<String?>` vs `List<OffenceCohort>` assertion in `SubjectAccessRequestServiceTest.kt`. Branch already exists on origin at commit `62490060` (plan doc only). Prompt below. Does NOT block Branch 2 — they can run in parallel. |
| 2. `APG-2580/attendance-codes-to-descriptions` | [Branch 2](./APG-2580-branch-2-attendance-codes-to-descriptions.md) | ⏳ Ready to start (prompt below) | — | — | Base branch: `APG-2580/remove-pii-and-duplicate-sections` (or `main` after #860 merges). Independent of the 1.5 sweep-up. |
| 3. `APG-2580/enum-display-names` | [Branch 3](./APG-2580-branch-3-enum-display-names.md) | 🛑 Blocked on Branch 2 | — | — | Awaiting product sign-off on `InterventionType` display names |
| 4. `APG-2580/attendance-session-note-restructure` | [Branch 4](./APG-2580-branch-4-attendance-session-note-restructure.md) | 🛑 Blocked on Branch 3 | — | — | — |

Legend: ⏳ Not started · 🟡 In progress · 🔎 Awaiting planning-agent review · ✅ Merged · 🛑 Blocked

---

## Open questions to resolve before / during delivery

- [ ] **[Branch 3]** Confirm `InterventionType` display names with the service owner:
  - `SI` → `"Structured Intervention"` ?
  - `ACP` → `"Accredited Programme"` ?
  - `CRS` → `"Commissioned Rehabilitative Service"` ?
  - `TOOLKITS` → `"Toolkits"` ?
- [ ] **[Branch 4]** Team happy for the test fixture to render note `recordedBy` as `null` / "No Data Held" because the integration test factory doesn't populate `createdByFullName`? (Production data will be set correctly.)

---

## Implementer prompt – Branch 1: Remove PII and duplicate sections

**Copy everything from `>>>>> BEGIN PROMPT` down to `<<<<< END PROMPT` into a fresh agent session (new Copilot chat / different tool). The implementer is expected to have shell access + the ability to edit files in this repo.**

>>>>> BEGIN PROMPT

You are an implementation agent delivering branch 1 of Jira ticket **APG-2580** in the repo `hmpps-accredited-programmes-manage-and-deliver-api`. The branch has been fully planned already — your job is to execute the plan exactly, run the tests, and report back.

**Repo:** `/Users/raby.whyte/code/hmpps-accredited-programmes-manage-and-deliver-api`

### Setup

1. Ensure you are on `main`, then create and check out the branch:
   ```bash
   git checkout main && git pull
   git checkout -b APG-2580/remove-pii-and-duplicate-sections
   ```

### Plan to execute

Read the full plan here and follow it verbatim:

`docs/branches/APG-2580-branch-1-remove-pii-and-duplicate-sections.md`

That doc lists every file to delete, every file to modify, and the exact new file contents. It also includes updates to `SubjectAccessRequestServiceTest.kt`.

**One override vs. that plan:** For the "regenerate fixtures" step at the end, do NOT run the manual `SAR_GENERATE_ACTUAL=true ./gradlew …` + `cp` sequence. Instead run:

```bash
./scripts/local-scripts/regenerate-sar-snapshots.sh
```

This script does both steps and cleans up the temporary `.log` files. See `docs/how-to/update-sar-tests.md` for context.

### Acceptance criteria (all must be true before you report back)

- [ ] Files deleted:
  - `src/main/kotlin/.../api/model/subjectAccessRequest/SubjectAccessRequestGroupWaitlistItemView.kt`
  - `src/main/kotlin/.../api/model/subjectAccessRequest/SubjectAccessRequestReferralCaseListItemView.kt`
- [ ] `SubjectAccessRequestReferral.kt` no longer contains `crn`, `dateOfBirth`, `personName` (data class + `toApi()`).
- [ ] `SubjectAccessRequestContent.kt` has only the `referrals` field.
- [ ] `SubjectAccessRequestService.kt` no longer references `GroupWaitlistItemViewRepository` or `ReferralCaseListItemRepository` (constructor, imports, private helpers all gone).
- [ ] `sar_template.mustache`:
  - The 3 PII rows (Person Name, CRN, Date of Birth) are removed from the referral details table.
  - The `Group Waitlist Items` and `Referral Case List Items` sections (and their `{{^…}}` fallbacks) are removed entirely — the file must end after `{{/referrals}}`.
- [ ] `SubjectAccessRequestServiceTest.kt` updated per Branch 1 doc §5 (imports, mocks, constructor, PII-field assertions, waitlist/caselist blocks, verify calls). File compiles.
- [ ] `./gradlew ktlintCheck` passes (pre-commit hook auto-formats on commit, but run this explicitly).
- [ ] Fixtures regenerated via `./scripts/local-scripts/regenerate-sar-snapshots.sh`.
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes without `SAR_GENERATE_ACTUAL`.
- [ ] `./gradlew test --tests "*SubjectAccessRequestServiceTest*"` passes.
- [ ] `./gradlew build` (full build incl. all tests + ktlint) passes.

### Push and open PR

```bash
git add -A
git commit -m "APG-2580: Remove PII and duplicate waitlist/caselist sections from SAR"
git push -u origin APG-2580/remove-pii-and-duplicate-sections
```

Open a PR against `main` titled: `APG-2580 Remove PII and duplicate sections from SAR (community)`

### Report back to the planning agent

Reply in this exact structure so the planning agent can update the tracker:

```
BRANCH: 1
BRANCH NAME: APG-2580/remove-pii-and-duplicate-sections
PR URL: <url or "not yet opened">

FILES DELETED:
  - <path>

FILES MODIFIED (with 1-line summary each):
  - <path>: <summary>

TEST RESULTS:
  - ktlintCheck: PASS / FAIL (<paste failure summary if any>)
  - SarContractIntegrationTest: PASS / FAIL
  - SubjectAccessRequestServiceTest: PASS / FAIL
  - Full build (./gradlew build): PASS / FAIL

FIXTURE DIFF SUMMARY:
  - sar-api-response.json: <one line: what changed>
  - sar-expected-render-result.html: <one line: what changed>

DEVIATIONS FROM PLAN:
  - <list any place you did not follow the plan literally, or "none">

QUESTIONS FOR PLANNING AGENT:
  - <list any, or "none">
```

Do NOT proceed to Branch 2. Stop after reporting.

<<<<< END PROMPT

## Implementer prompt – Sweep-up (post-PR #860): Delete orphaned SAR factories & fix cohort assertion

**When to run this prompt:** Only after PR #860 (`APG-2580/remove-pii-and-duplicate-sections`) has been merged into `main`. Runs independently of Branch 2 and does not block it.

**Copy everything from `>>>>> BEGIN PROMPT` down to `<<<<< END PROMPT` into a fresh agent session.**

>>>>> BEGIN PROMPT

You are an implementation agent delivering the sweep-up follow-up to **APG-2580 PR #860** in the repo `hmpps-accredited-programmes-manage-and-deliver-api`. Two small changes surfaced by the 9-eyes review of PR #860; they are fully planned already — your job is to execute the plan exactly, run the tests, and report back.

**Repo:** `/Users/raby.whyte/code/hmpps-accredited-programmes-manage-and-deliver-api`

### Prerequisites (verify before doing anything else)

1. `origin/main` contains the PR #860 merge:
   ```bash
   git fetch origin
   git log --oneline origin/main | grep "APG-2580: Remove PII"
   ```
   If empty, STOP — PR #860 has not merged yet.

2. Both factories are still orphaned on `origin/main` after the merge:
   ```bash
   git grep -n "GroupWaitlistItemViewEntityFactory\|ReferralCaseListItemViewEntityFactory" origin/main -- src/ \
     | grep -v "factory/GroupWaitlistItemViewEntityFactory.kt\|factory/ReferralCaseListItemViewEntityFactory.kt"
   ```
   Must be empty. If not, STOP and re-plan.

### Setup

```bash
git fetch origin
git checkout APG-2580/cleanup-orphaned-sar-factories
git rebase origin/main
```

The branch already exists (commit `62490060`, plan doc only). Rebase brings in the Branch 1 code so the deletions and assertion fix apply cleanly.

### Plan to execute

Read the full plan here and follow it verbatim:

`docs/branches/APG-2580-cleanup-sar-factories-plan.md`

That doc lists both files to delete, the exact one-line assertion change, verification steps, and a self-delete instruction for the plan doc itself.

### Acceptance criteria (all must be true before you report back)

- [ ] Files deleted:
  - `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/GroupWaitlistItemViewEntityFactory.kt`
  - `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/ReferralCaseListItemViewEntityFactory.kt`
  - `docs/branches/APG-2580-cleanup-sar-factories-plan.md` (this plan self-deletes on execution)
- [ ] `SubjectAccessRequestServiceTest.kt` cohort assertion RHS changed from `.map { it.cohort }` to `.map { it.cohort.displayName }` (single-line diff, no import changes).
- [ ] `grep -rn "GroupWaitlistItemViewEntityFactory\|ReferralCaseListItemViewEntityFactory" src/` returns empty (confirms no dangling imports).
- [ ] `./gradlew ktlintCheck` passes.
- [ ] `./gradlew test --tests "*SubjectAccessRequestServiceTest*"` passes.
- [ ] `./gradlew build` (full build) passes.
- [ ] No SAR fixture regeneration needed (nothing observable through SAR changes).

### Push and open PR

```bash
git add -A
git commit -m "APG-2580: Delete orphaned SAR test factories and fix cohort assertion"
git push --force-with-lease -u origin APG-2580/cleanup-orphaned-sar-factories
```

(`--force-with-lease` because we rebased; the branch on origin currently has only the plan doc commit.)

Open a PR against `main` titled: `APG-2580 Delete orphaned SAR test factories and fix cohort assertion (sweep-up)`

Body: link to PR #860 as the parent; note this is a sweep-up of two review-observed items with no runtime impact.

### Report back to the planning agent

Reply in this exact structure:

```
BRANCH: 1.5 sweep-up
BRANCH NAME: APG-2580/cleanup-orphaned-sar-factories
PR URL: <url>

FILES DELETED:
  - <path>

FILES MODIFIED (with 1-line summary each):
  - <path>: <summary>

TEST RESULTS:
  - ktlintCheck: PASS / FAIL
  - SubjectAccessRequestServiceTest: PASS / FAIL
  - Full build (./gradlew build): PASS / FAIL

DEVIATIONS FROM PLAN:
  - <list any, or "none">

QUESTIONS FOR PLANNING AGENT:
  - <list any, or "none">
```

Do NOT proceed to Branch 2 (it is being run separately). Stop after reporting.

<<<<< END PROMPT

---

## Implementer prompt – Branch 2: Replace attendance outcome codes with descriptions

**Copy everything from `>>>>> BEGIN PROMPT` down to `<<<<< END PROMPT` into a fresh agent session.**

>>>>> BEGIN PROMPT

You are an implementation agent delivering branch 2 of Jira ticket **APG-2580** in the repo `hmpps-accredited-programmes-manage-and-deliver-api`. The branch has been fully planned already — your job is to execute the plan exactly, run the tests, and report back.

**Repo:** `/Users/raby.whyte/code/hmpps-accredited-programmes-manage-and-deliver-api`

### Setup

Branch 1 (`APG-2580/remove-pii-and-duplicate-sections`, PR #860) has been merged / is in review. Base this branch on it so the fixture regeneration works against Branch 1's already-shrunk fixtures:

```bash
git fetch origin
git checkout APG-2580/remove-pii-and-duplicate-sections
git pull
git checkout -b APG-2580/attendance-codes-to-descriptions
```

If PR #860 has already merged to `main` by the time you start, use `main` as the base instead:

```bash
git checkout main && git pull
git checkout -b APG-2580/attendance-codes-to-descriptions
```

### Plan to execute

Read the full plan here and follow it verbatim:

`docs/branches/APG-2580-branch-2-attendance-codes-to-descriptions.md`

That doc changes exactly 2 files:
1. `SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt` — rename `outcomeTypeCode: String` → `outcomeDescription: String?`, map from `description` not `code.name`.
2. `sar_template.mustache` — the one attendance-outcome `<td>` line switches from `outcomeType.outcomeTypeCode` to `outcomeType.outcomeDescription`.

### Overrides vs. the plan

1. **Fixture regeneration:** ignore the manual `SAR_GENERATE_ACTUAL=true ./gradlew …` + `cp` sequence in the plan. Run this instead:
   ```bash
   ./scripts/local-scripts/regenerate-sar-snapshots.sh
   ```
2. **Downstream test files (mandatory pre-flight, added after Branch 1 discovery):** before you commit, run these greps and update any hit sites that reference the renamed field:
   ```bash
   grep -rn "outcomeTypeCode" src/ scripts/ docs/
   ```
   Expected hit sites (all correct after your changes): the DTO you renamed, `sar_template.mustache`, the regenerated `sar-api-response.json`, and doc files. If a Kotlin test file hits, that's a Branch-1-style deviation — update it and note in the report.
3. **Update the Branch 2 verification-checklist item** in the branch doc that says "run a grep" — you've now done it as part of the pre-flight above.

### Acceptance criteria (all must be true before you report back)

- [ ] `SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt` renamed field + updated mapper matches the plan verbatim (field is nullable `String?`).
- [ ] `sar_template.mustache` line updated to `{{ optionalValue outcomeType.outcomeDescription }}`.
- [ ] Grep for `outcomeTypeCode` returns zero hits in `src/**/*.kt` (docs and fixture-relative discussions are OK).
- [ ] `./gradlew ktlintCheck` passes.
- [ ] `./scripts/local-scripts/regenerate-sar-snapshots.sh` completes without errors and the two SAR fixture files change.
- [ ] `sar-api-response.json` diff shows `"outcomeTypeCode":"..."` → `"outcomeDescription":"..."` inside the `outcomeType` object.
- [ ] `sar-expected-render-result.html` diff shows the attendance outcome cell now renders human-readable text (e.g. `Attended - Complied` or `Attended`) instead of `ATTC`.
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes without `SAR_GENERATE_ACTUAL`.
- [ ] `./gradlew build` passes.

### Push and open PR

```bash
git add -A
git commit -m "APG-2580: Replace attendance outcome codes with descriptions in SAR"
git push -u origin APG-2580/attendance-codes-to-descriptions
```

Open a PR against `main` (if Branch 1 has merged) or against `APG-2580/remove-pii-and-duplicate-sections` (if it hasn't yet), titled: `APG-2580 Replace attendance outcome codes with descriptions (community)`

Include a note in the PR description that this stacks on top of PR #860 if the base is that branch.

### Report back to the planning agent

Reply in this exact structure:

```
BRANCH: 2
BRANCH NAME: APG-2580/attendance-codes-to-descriptions
BASE BRANCH: main OR APG-2580/remove-pii-and-duplicate-sections
PR URL: <url or "not yet opened">

FILES MODIFIED (with 1-line summary each):
  - <path>: <summary>

PRE-FLIGHT GREP RESULTS:
  - grep "outcomeTypeCode": <list any Kotlin hits or "none in Kotlin sources">

TEST RESULTS:
  - ktlintCheck: PASS / FAIL
  - SarContractIntegrationTest: PASS / FAIL
  - Full build (./gradlew build): PASS / FAIL

FIXTURE DIFF SUMMARY:
  - sar-api-response.json: <one line: what changed>
  - sar-expected-render-result.html: <one line: what changed, incl. the actual human-readable value now shown>

DEVIATIONS FROM PLAN:
  - <list any, or "none">

QUESTIONS FOR PLANNING AGENT:
  - <list any, or "none">
```

Do NOT proceed to Branch 3. Stop after reporting.

<<<<< END PROMPT

---

## Implementer prompt – Branch 3

*Will be generated by the planning agent after Branch 2 is confirmed ✅. Note: needs the answer to the `InterventionType` display-name question above before it can start.*

## Implementer prompt – Branch 4

*Will be generated by the planning agent after Branch 3 is confirmed ✅.*

---

## Change log

- **2026-08-18** – Tracker created by planning agent. Full re-validation of all 4 branch docs against `main`. Everything matches; only clarifications listed above are needed. Branch 1 implementer prompt released.
- **2026-08-19** – Branch 1 implemented (commit `679c35bd`, PR [#860](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api/pull/860)). All acceptance criteria met. 1 planned deviation: `HmppsSubjectAccessRequestControllerIntegrationTest.kt` lost 4 lines of key-presence assertions on the removed `groupWaitlistItemViews` / `referralCaseListItemViews` keys. Diff verified: 10 files changed, +2/−247. Clarification #6 added. Branch 2 implementer prompt released with new mandatory pre-flight grep step.
- **2026-08-19 (later)** – Merged `main` into Branch 1 to keep it current; CI failed on `SarContractIntegrationTest.SAR API should return expected data` due to `referralCohortHistories` reordering (2 rows swapped). Root cause: `MutableSet` + `@OrderBy(createdAt DESC)` with tied timestamps + Hibernate/byte-buddy version bump from Gradle deps PR #848. **Fix pushed as commit `bab0cb03`** on Branch 1: deterministic secondary sort by `id` in `SubjectAccessRequestReferral.toApi()`. Diff: 1 file, +5/−1. No fixture change. Clarification #7 added so Branches 2–4 pre-emptively check any Set-typed SAR DTO field they touch.

- **2026-08-19** – 9-eyes review of PR #860 surfaced two follow-ups: (a) `GroupWaitlistItemViewEntityFactory` and `ReferralCaseListItemViewEntityFactory` now have zero call-sites (correction #5 wording was outdated); (b) `SubjectAccessRequestServiceTest.kt` line 125 has a pre-existing `List<String?>` vs `List<OffenceCohort>` type-mismatch assertion. Both scoped to a standalone sweep-up branch `APG-2580/cleanup-orphaned-sar-factories` with a validated plan doc committed there. Execution deferred until PR #860 merges to `main`. Branch 1 doc §6/§7 amended to point at sweep-up. Correction #7 added. No impact on Branches 2–4.
- **2026-08-19** – Sweep-up formalised: plan doc `APG-2580-cleanup-sar-factories-plan.md` mirrored onto planning-docs for discoverability; tracker gains a status-board row (1.5) between Branches 1 and 2, an implementer prompt block in the same shape as Branches 1/2, and the reference-documents table now links to the plan doc. Correction #7 now anchors at the new prompt. Plan doc technical claims re-verified against Branch 1 head (`679c35bd`): factories orphaned, cohort assertion still at line 125, `OffenceCohort.displayName` still `enum(val displayName: String)`, DTO `.cohort: String?` mapped from `cohort.displayName`. No changes to Branches 2–4.
