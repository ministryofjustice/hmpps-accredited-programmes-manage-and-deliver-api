# APG-2580 Sweep-up – Fix Cohort Assertion in SubjectAccessRequestServiceTest

**Branch:** `APG-2580/cleanup-orphaned-sar-factories`  *(branch name retained for continuity; the scope was reduced after planning — see box below)*
**Base:** `main` (targets `main` after PR #860 / Branch 1 is merged — now confirmed merged as `4adc4b08`)
**Trigger:** Only execute this plan **after** PR #860 (`APG-2580/remove-pii-and-duplicate-sections`) is merged into `main`.
**Author:** Copilot planning session, re-validated against workspace on 2026-08-20 after implementer preflight halt.

> This document is the plan-of-record. When the user triggers execution, the implementer agent (this session or a fresh one) should follow this doc **verbatim**. **No guesswork** — every claim below has been validated against the checked-out codebase and is captured with exact file paths, line numbers, and code snippets.

---

## ⚠️  Scope reduction (2026-08-20)

The original plan for this branch included deleting two "orphaned" test factories:

- `GroupWaitlistItemViewEntityFactory.kt`
- `ReferralCaseListItemViewEntityFactory.kt`

**Both deletions have been removed from scope.** Reasons, captured at execution time by the implementer preflight check + planning re-validation on `origin/main`:

1. **`GroupWaitlistItemViewEntityFactory` is no longer orphaned.** PR #865 (`APG-2602/add-lao-to-view-allocations-and-waitlist`) merged after the sweep-up was planned. It adds two call-sites in `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/ProgrammeGroupServiceTest.kt` (import at line 20, factory instantiation at lines 131 and 222). Deletion would break that test.
2. **`ReferralCaseListItemViewEntityFactory` is technically still orphaned**, but empirical evidence from its sibling shows that "orphan" status in this repo can flip inside 24 hours. Both factories were originally created by the same commit (PR #494, `Apg 1713/build sar endpoint`) as reusable test scaffolding. There is active team work in the caselist / LAO area (see PRs #813, #847, #862, #863, #865) that could plausibly reach for this factory next. The cost of keeping it = 49 lines of test-only code; the cost of hasty deletion = future churn if someone recreates it from scratch.

**Lesson for future planning:** grep-based "orphan" checks are point-in-time snapshots. In an active codebase, orphan status can flip within a day. Do not scope deletion of test scaffolding on a snapshot — require either a real motivating problem (compile error, misleading test, security concern) or evidence of prolonged disuse. This lesson is captured in the delivery tracker's Corrections / clarifications section.

**What remains in scope:** the single-line cohort-assertion fix in `SubjectAccessRequestServiceTest.kt`, which is a real bug fix and stands entirely on its own merit.

---

## Why this branch exists (revised)

The 9-eyes review of PR #860 surfaced a pre-existing type-mismatch assertion in `SubjectAccessRequestServiceTest.kt`. It compares `List<String?>` (DTO `.cohort`) to `List<OffenceCohort>` (entity `.cohort`) — silently always-false for non-empty cohort sets. The test only passes today because the referral fixture happens to have an empty cohort set, so both sides collapse to `List<>` of size 0 and the assertion provides zero coverage.

This branch fixes that assertion so the compared lists have the same element type. No production code changes, no SAR fixture regeneration, one-line diff.

---

## Prerequisites at execution time

Before opening this branch's PR the implementer must confirm:

- [ ] `origin/main` contains the Branch 1 merge:
  ```bash
  git fetch origin
  git log --oneline origin/main | grep "#860"
  ```
  Should return `4adc4b08 APG-2580 Remove PII and duplicate sections from SAR (community) (#860)`. If empty, STOP — PR #860 has not merged yet.

  *(Note: earlier drafts of this doc used the pattern `"APG-2580: Remove PII"` with a colon — that was wrong. GitHub's default "Squash and merge" title has no colon. Use `"#860"` — it is uniquely identifying and immune to title reformatting.)*

- [ ] `git fetch origin && git rebase origin/main` completes cleanly on this branch. The sweep-up branch only touches `docs/branches/APG-2580-cleanup-sar-factories-plan.md` (a new file), so rebase over post-Branch-1 `main` is trivial.

---

## Codebase validation (captured on 2026-08-20 vs `origin/main`)

### Validation — cohort assertion is comparing incompatible types

**Current line (`src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt` line 125 post-merge):**

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort })
```

- LHS: `resultContent.referrals[0]` is a `SubjectAccessRequestReferral`, so `referralCohortHistories: MutableSet<SubjectAccessRequestReferralCohortHistory>`. Its `cohort` field is declared:

  `src/main/kotlin/.../api/model/subjectAccessRequest/SubjectAccessRequestReferralCohortHistory.kt`:
  ```kotlin
  data class SubjectAccessRequestReferralCohortHistory(
    val createdBy: String?,
    val createdAt: LocalDateTime?,
    val cohort: String?,   // ← String, mapped from entity.cohort.displayName
  )

  fun ReferralCohortHistoryEntity.toApi() = SubjectAccessRequestReferralCohortHistory(
    createdBy = createdBy,
    createdAt = createdAt,
    cohort = cohort.displayName,
  )
  ```
  LHS map result → `List<String?>`.

- RHS: `referralEntity1.referralCohortHistories` is `MutableSet<ReferralCohortHistoryEntity>`. Its `cohort` field:

  `src/main/kotlin/.../entity/ReferralCohortHistoryEntity.kt` (line 40):
  ```kotlin
  @Column(name = "cohort")
  @Enumerated(EnumType.STRING)
  var cohort: OffenceCohort,
  ```
  RHS map result → `List<OffenceCohort>`.

`OffenceCohort` is an enum with a `displayName` property:

`src/main/kotlin/.../api/model/OffenceCohort.kt`:
```kotlin
enum class OffenceCohort(val displayName: String) {
  SEXUAL_OFFENCE("Sexual offence"),
  GENERAL_OFFENCE("General offence"),
  ;
  // …
}
```

So `List<String?>.isEqualTo(List<OffenceCohort>)` is **always false** for non-empty inputs. It only passes today because in that test scenario `referralEntity1` is produced from `ReferralEntityFactory().withProgrammeGroupMemberships(…).withStatusHistories(…).…produce()` — no `withReferralCohortHistories(…)` builder is called, so the set is empty and both sides collapse to `List<>` of size 0. The assertion silently provides no coverage.

The fix mirrors how other enum→String comparisons are already done in the same test (e.g. `interventionType.name`, `sourcedFrom?.name`):

**Target line (post-fix, line 125):**

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort.displayName })
```

This aligns exactly with the mapper (`ReferralCohortHistoryEntity.toApi()` uses `cohort.displayName`) so the assertion now proves the mapper is doing the right thing.

---

## Exact changes to apply

### Change 1 — fix cohort assertion

File: `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SubjectAccessRequestServiceTest.kt`

Replace (line 125 post-Branch-1-merge; re-verify line number after rebase):

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort })
```

with:

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort.displayName })
```

Diff-wise: only the RHS closure changes from `it.cohort` to `it.cohort.displayName`. No import changes required (`OffenceCohort` is not referenced directly; only its `displayName` property is used through `it.cohort.displayName`).

### Change 2 — delete this plan doc

```bash
git rm docs/branches/APG-2580-cleanup-sar-factories-plan.md
```

Rationale: the doc's job is done once the PR is executed. Keep the git history clean of ephemeral plan docs. (The mirror on the `APG-2580/planning-docs` branch is the durable record and remains untouched.)

---

## Verification steps (must all pass before pushing)

Run in this order from the repo root:

```bash
# 1. Static analysis
./gradlew ktlintCheck

# 2. Targeted unit test (the one whose assertion we're fixing)
./gradlew test --tests "*SubjectAccessRequestServiceTest*"

# 3. Full build (defensive; no reason to expect breakage from a one-line assertion fix)
./gradlew build
```

Expected outcomes:

- `ktlintCheck` — **PASS**.
- `SubjectAccessRequestServiceTest` — both tests **PASS** (unchanged count; the assertion now actually asserts something meaningful, but with the current empty cohort-history set both sides are still `List<>` of size 0, so the test still passes).
- `./gradlew build` — full test suite **PASS**.

No SAR fixture regeneration is required (this change is test-side only; nothing observable through the SAR endpoint changes).

---

## Commit / push / PR

```bash
git add -A
git commit -m "APG-2580: Fix cohort assertion in SubjectAccessRequestServiceTest

The assertion compared List<String?> (DTO .cohort, mapped from
OffenceCohort.displayName) with List<OffenceCohort> (entity .cohort). Always
false for non-empty sets; only passed today because the test's referralEntity1
has an empty cohort-history set. Fix aligns RHS with the mapper output so the
assertion actually validates the mapping.

Surfaced by the 9-eyes review of PR #860. Original sweep-up scope also intended
to delete two orphaned SAR test factories; the GroupWaitlistItemViewEntityFactory
was picked up by PR #865 (LAO waitlist work) within a day of being marked
orphaned, so both factory deletions were dropped from scope to avoid churn."

git push --force-with-lease -u origin APG-2580/cleanup-orphaned-sar-factories
```

(`--force-with-lease` because we rebased onto post-Branch-1 `main`; origin currently has only the plan-doc commit `98e49d7a` and needs to be overwritten with the executed branch.)

Open a PR against `main` titled:
`APG-2580 Fix cohort assertion in SubjectAccessRequestServiceTest (sweep-up)`

Body: link PR #860 as parent. Note this is the reduced-scope outcome of the 9-eyes review sweep-up — the factory deletions originally intended for this branch were dropped after `GroupWaitlistItemViewEntityFactory` was picked up by PR #865. Point at the delivery tracker (Corrections / clarifications #7 and change log).

---

## Post-execution reporting

Report back to the planning agent in the same shape used for Branch 1:

```
BRANCH: 1.5 sweep-up
BRANCH NAME: APG-2580/cleanup-orphaned-sar-factories
PR URL: <url>

FILES DELETED:
  - docs/branches/APG-2580-cleanup-sar-factories-plan.md

FILES MODIFIED:
  - src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt: cohort assertion RHS now maps to `.cohort.displayName`

TEST RESULTS:
  - ktlintCheck: PASS
  - SubjectAccessRequestServiceTest: PASS
  - Full build (./gradlew build): PASS

DEVIATIONS FROM PLAN:
  - <list any, or "none">

QUESTIONS FOR PLANNING AGENT:
  - <list any, or "none">
```

---

## Risk assessment

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Cohort assertion fix reveals a real mapping bug | Very Low | Test failure | Would be a genuine catch — investigate rather than revert. The mapper (`cohort.displayName`) is already exercised by the SAR contract snapshot fixture which passes. |
| Line 125 has shifted since planning | Low | Wrong-line edit / no match | Grep for the exact substring `referralCohortHistories.map { it.cohort }).isEqualTo` before editing; there is exactly one occurrence in the file. |
| Rebase over post-Branch-1 `main` conflicts | Very Low | Rebase abort | Only file touched on this branch so far is the plan doc (new file). Any conflict would be surprising; abort and re-plan if it happens. |
