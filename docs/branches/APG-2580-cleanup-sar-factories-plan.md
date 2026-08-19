# APG-2580 Sweep-up – Delete Orphaned SAR Factories & Fix Cohort Assertion

**Branch:** `APG-2580/cleanup-orphaned-sar-factories`
**Base:** `main` (targets `main` after PR #860 / Branch 1 is merged)
**Trigger:** Only execute this plan **after** PR #860 (`APG-2580/remove-pii-and-duplicate-sections`) is merged into `main`.
**Author:** Copilot planning session, validated against workspace on 2026-08-19.

> This document is the plan-of-record. When the user triggers execution, the implementer agent (this session or a fresh one) should follow this doc **verbatim**. **No guesswork** — every claim below has been validated against the checked-out codebase and is captured with exact file paths, line numbers, and code snippets.

---

## Why this branch exists

Follow-ups surfaced in the 9-eyes code review of PR #860. Two independent, low-risk changes that are cleaner as a small standalone PR than as a footnote on Branch 1:

1. **Delete now-orphaned test factories.** After PR #860 removes the SAR waitlist / caselist DTOs, two entity factories that were only ever instantiated by `SubjectAccessRequestServiceTest` become dead code.
2. **Fix a pre-existing type-mismatch assertion** in `SubjectAccessRequestServiceTest.kt` that IntelliJ / Kotlin compiler already flags as a warning: `List<String?>` vs `List<OffenceCohort>` — always false for non-empty cohort sets.

Neither change affects production behaviour, SAR output, or the SAR fixture snapshots.

---

## Prerequisites at execution time

Before opening this branch's PR the implementer must confirm:

- [ ] `origin/main` contains the Branch 1 merge (i.e. `git log --oneline origin/main | grep "APG-2580: Remove PII"` returns a commit).
- [ ] `git fetch origin && git rebase origin/main` completes cleanly on this branch (only the plan doc lives here so far, so this should be a no-op fast-forward or trivial rebase).
- [ ] `grep -rn "GroupWaitlistItemViewEntityFactory\|ReferralCaseListItemViewEntityFactory" src/ | grep -v "^src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/"` returns **empty**. If any usage has appeared since this plan was written (e.g. Branch 2/3/4 reintroduced them), **stop and re-plan**.

---

## Codebase validation (captured on 2026-08-19 vs `APG-2580/remove-pii-and-duplicate-sections` HEAD `679c35bd`)

### Validation 1 — `GroupWaitlistItemViewEntityFactory` is orphaned

Command run:

```bash
grep -rn "GroupWaitlistItemViewEntityFactory" src/ \
  | grep -v "factory/GroupWaitlistItemViewEntityFactory.kt"
```

Result on Branch 1 head: **empty** (0 call-sites).

File to be deleted:

```
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/GroupWaitlistItemViewEntityFactory.kt
```

Size: 59 lines. Underlying `GroupWaitlistItemViewEntity` remains in place and continues to be exercised by `GroupWaitlistViewRepositoryIntegrationTest`, `ProgrammeGroupService`, and `ProgrammeGroupServiceTest` — none of which use this factory. Verified by grep.

### Validation 2 — `ReferralCaseListItemViewEntityFactory` is orphaned

Command run:

```bash
grep -rn "ReferralCaseListItemViewEntityFactory" src/ \
  | grep -v "factory/ReferralCaseListItemViewEntityFactory.kt"
```

Result on Branch 1 head: **empty** (0 call-sites).

File to be deleted:

```
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/ReferralCaseListItemViewEntityFactory.kt
```

Size: 49 lines. Underlying `ReferralCaseListItemViewEntity` / `ReferralCaseListItemRepository` remain and continue to be used by `ReferralCaseListItemService` (production) and `CaseListControllerIntegrationTest` (via the repository directly, not this factory).

### Validation 3 — cohort assertion is comparing incompatible types

**Current line (Branch 1 HEAD, `SubjectAccessRequestServiceTest.kt` line 125):**

```kotlin
assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort })
  .isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort })
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
assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort })
  .isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort.displayName })
```

This aligns exactly with the mapper (`ReferralCohortHistoryEntity.toApi()` uses `cohort.displayName`) so the assertion now proves the mapper is doing the right thing.

---

## Exact changes to apply

### Change 1 — delete `GroupWaitlistItemViewEntityFactory.kt`

```bash
git rm src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/GroupWaitlistItemViewEntityFactory.kt
```

### Change 2 — delete `ReferralCaseListItemViewEntityFactory.kt`

```bash
git rm src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/ReferralCaseListItemViewEntityFactory.kt
```

### Change 3 — fix cohort assertion

File: `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SubjectAccessRequestServiceTest.kt`

Replace (line 125 as of Branch 1 HEAD; re-verify line number after any rebase):

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort })
```

with:

```kotlin
    assertThat(resultContent.referrals[0].referralCohortHistories.map { it.cohort }).isEqualTo(referralEntity1.referralCohortHistories.map { it.cohort.displayName })
```

Diff-wise: only the RHS closure changes from `it.cohort` to `it.cohort.displayName`. No import changes required (`OffenceCohort` is not referenced directly; only its `displayName` property is used through `it.cohort.displayName`).

### Change 4 — delete this plan doc

```bash
git rm docs/branches/APG-2580-cleanup-sar-factories-plan.md
```

Rationale: the doc's job is done once the PR is executed. Keep the git history clean of ephemeral plan docs. (The Branch 1 planning docs on `APG-2580/planning-docs` remain untouched.)

---

## Verification steps (must all pass before pushing)

Run in this order from the repo root:

```bash
# 1. Static analysis
./gradlew ktlintCheck

# 2. Targeted unit test (the only test that exercised those factories / the fixed assertion)
./gradlew test --tests "*SubjectAccessRequestServiceTest*"

# 3. Full build (validates that nothing else transitively depended on the deleted factories)
./gradlew build
```

Expected outcomes:

- `ktlintCheck` — **PASS**.
- `SubjectAccessRequestServiceTest` — both tests **PASS** (unchanged count).
- `./gradlew build` — full 874-test PASS, 0 failed, 9 skipped (same profile as Branch 1 merge state).

If step 3 surfaces a `Unresolved reference: GroupWaitlistItemViewEntityFactory` or `Unresolved reference: ReferralCaseListItemViewEntityFactory` in any file, **stop** — the prerequisite check missed a caller. Grep for the reference, restore the factory, and re-plan.

No SAR fixture regeneration is required (Change 3 does not change SAR output; Changes 1, 2, 4 don't touch production code).

---

## Commit / push / PR

```bash
git add -A
git commit -m "APG-2580: Delete orphaned SAR test factories and fix cohort assertion

- Delete GroupWaitlistItemViewEntityFactory + ReferralCaseListItemViewEntityFactory
  (orphaned after PR #860 removed the SAR waitlist / caselist DTOs).
- Fix SubjectAccessRequestServiceTest cohort assertion so the compared lists have
  the same element type (String vs String) instead of String vs OffenceCohort enum.

Both changes were surfaced by the 9-eyes review of PR #860."

git push --force-with-lease -u origin APG-2580/cleanup-orphaned-sar-factories
```

(`--force-with-lease` because we rebased onto post-Branch-1 `main`; origin currently has only the plan-doc commit `62490060` and needs to be overwritten with the executed branch.)

Open a PR against `main` titled:
`APG-2580 Delete orphaned SAR test factories and fix cohort assertion`

Body: link to PR #860, note that this is a sweep-up of two review-observed items with no runtime impact.

---

## Post-execution reporting

Report back to the planning agent in the same shape used for Branch 1:

```
BRANCH: 1.5 sweep-up
BRANCH NAME: APG-2580/cleanup-orphaned-sar-factories
PR URL: <url>

FILES DELETED:
  - src/test/kotlin/.../factory/GroupWaitlistItemViewEntityFactory.kt
  - src/test/kotlin/.../factory/ReferralCaseListItemViewEntityFactory.kt
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
| A downstream branch (Branch 2/3/4) reintroduces a factory usage before this PR lands | Low | Test failure at build time | Prerequisite grep check documented above; run before committing. |
| Cohort assertion fix reveals a real mapping bug | Very Low | Test failure | Would be a genuine catch — investigate rather than revert. The mapper (`cohort.displayName`) is already exercised by the SAR contract snapshot fixture which passes. |
| Factory deletion removes coverage of `GroupWaitlistItemViewEntity` / `ReferralCaseListItemViewEntity` builders | None | – | Entities are still covered by their repository/service integration tests which construct them directly. Factories were unused syntactic sugar. |

