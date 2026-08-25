# APG-2580 Follow-up 6 — `programmeGroupMemberships` `@OrderBy` + `statusHistories` symmetry fix

**Status:** Not started
**Priority:** Medium (`ReferralStatusService` sites drive a public event) / Low (`TelemetryService` sites are telemetry only)
**Blocked by:** Nothing — independent of follow-up 5, can run in parallel or after
**Estimated size:** ~2 hours
**Branch to create:** `APG-2580/membership-and-status-history-tiebreaks`

Bundles two thematically-linked "order-sensitive read hardening" items:

- **Part A** — Add `@OrderBy("createdAt DESC")` to
  `ReferralEntity.programmeGroupMemberships` + belt-and-braces in-Kotlin
  tiebreak at the two consumer sites in `ReferralStatusService`.
- **Part B** — Mirror the `referralCohortHistories` fix (PR #880) on
  `statusHistories` in `TelemetryService.logToAppInsights` for the
  `referralStatus` and `fromStatus` telemetry properties.

Both parts follow the same idiom (JPA-level order + in-Kotlin
`sortedWith(...).firstOrNull()` for tie determinism, per audit correction #8).

---

## Motivation

The follow-up-4 audit surfaced these three call-sites as silently
order-sensitive on `createdAt` ties. Unlike the follow-up-5 attendance sites,
these fields are NOT `SessionAttendanceEntity` so cannot share
`latestByCreatedAt()`.

### Part A — `programmeGroupMemberships`

- `ReferralEntity.programmeGroupMemberships` is `MutableSet<ProgrammeGroupMembershipEntity>`
  with **no `@OrderBy`** (verified `ReferralEntity.kt:144` on `main`).
- Two consumers do `.maxByOrNull { it.createdAt }` and treat the result as
  "the referral's current group membership":
  - `ReferralStatusService.kt:113` — `getCompletionDataForReferral` — drives
    which `postProgrammeReviewSession` is looked up and whose attendance
    controls completion.
  - `ReferralStatusService.kt:157` — `hasValidPostProgrammeReviewAttendance` —
    gates whether `ReferralProgrammeCompleteEvent` fires.
- **Functional impact of a wrong pick:** two memberships tied on `createdAt`
  (rare but possible if a referral is bulk-re-enrolled in the same second, or
  during a data-migration backfill) can cause the completion event to fire
  against the wrong group, or the wrong session's attendance to be inspected.

### Part B — `TelemetryService.statusHistories`

- `ReferralEntity.statusHistories` is `MutableList<ReferralStatusHistoryEntity>`
  (NOT `MutableSet`) with `@OrderBy("createdAt DESC")` (verified
  `ReferralEntity.kt:73–74` on `main`).
- Two consumer sites in `TelemetryService.logToAppInsights`:
  - `referralStatus` → `statusHistories?.firstOrNull()?.referralStatusDescription?.description`
  - `fromStatus` → `statusHistories?.firstOrNull()?.referralStatusDescription?.id?.toString()`
- **Strictly out of the Set-audit scope** (it's a List), but shares the same
  tie-risk shape as the `referralCohortHistories` fix already shipped in
  the same file by PR #880: ties on `createdAt` fall through to DB
  result-set order (or Hibernate's LinkedHashSet fallback), which is not
  a stable contract.
- **Functional impact:** telemetry only, no user-facing decisions —
  low. Doing it for symmetry and because the fix is a two-line diff in
  the same file that already got the cohort fix.

---

## Scope — 3 call-sites (verified against source, 2026-08-25)

### Part A

| # | Location | Current code | Refactor |
|---|---|---|---|
| A.1 | `entity/ReferralEntity.kt:144` — field declaration | `var programmeGroupMemberships: MutableSet<ProgrammeGroupMembershipEntity> = mutableSetOf()` | Add `@OrderBy("createdAt DESC")` on the line above |
| A.2 | `service/ReferralStatusService.kt:113` | `referral.programmeGroupMemberships.maxByOrNull { it.createdAt }` | `referral.programmeGroupMemberships.sortedWith(compareByDescending<ProgrammeGroupMembershipEntity> { it.createdAt }.thenBy { it.programmeGroup.code }.thenBy { it.createdByUsername }).firstOrNull()` |
| A.3 | `service/ReferralStatusService.kt:157` | same shape | same refactor |

### Part B

| # | Location | Current code | Refactor |
|---|---|---|---|
| B.1 | `service/TelemetryService.kt` — `referralStatus` telemetry property (currently `statusHistories?.firstOrNull()?.referralStatusDescription?.description`) | direct `?.firstOrNull()` | `?.sortedWith(compareByDescending<ReferralStatusHistoryEntity> { it.createdAt }.thenBy { it.createdBy }.thenBy { it.referralStatusDescription.description })?.firstOrNull()` |
| B.2 | `service/TelemetryService.kt` — `fromStatus` telemetry property (currently `statusHistories?.firstOrNull()?.referralStatusDescription?.id?.toString()`) | direct `?.firstOrNull()` | same as B.1 |

### Verified consumer safety for Part A (`@OrderBy` addition is safe)

Full consumer scan (`grep -rn "programmeGroupMemberships" src/main/kotlin`,
excluding `import` lines and the field declaration itself), 2026-08-25:

| Consumer | Access pattern | Safe with `@OrderBy("createdAt DESC")`? |
|---|---|---|
| `repository/ReferralRepository.kt:16` | `LEFT JOIN FETCH r.programmeGroupMemberships` | Yes — fetch, order-agnostic |
| `api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt:54` | Uses `.sortedWith(...)` explicitly (PR #877) | Yes — its own re-sort dominates |
| `api/model/programmeGroup/GroupDetailsResponse.kt:142` | `.count { it.deletedAt == null }` on a different field (`programmeGroup.programmeGroupMemberships`) — same class, different owner-entity | Yes — order-agnostic |
| `service/ReferralStatusService.kt:113,157` | `.maxByOrNull { it.createdAt }` — the two sites we're fixing | Yes — `@OrderBy` matches the intent |
| `service/ProgrammeGroupMembershipService.kt:75` | `.add(...)` — write, not read | Yes — write |
| `service/ScheduleService.kt:318–322` | local `val programmeGroupMemberships` (different scope — repository query result, not the entity field) | N/A — not the same collection |

**Conclusion:** adding `@OrderBy("createdAt DESC")` on the entity field is a
zero-behavioural-change addition for existing consumers, and gives A.2 + A.3
the DB-level ordering they want. The in-Kotlin `sortedWith(...)` re-sort is
belt-and-braces for `createdAt` ties (mirrors audit correction #8's rationale).

### Tiebreak-key nullability

**`ProgrammeGroupMembershipEntity`** (verified from source, 2026-08-25):

- `createdAt: LocalDateTime` — non-null (default value)
- `programmeGroup: ProgrammeGroupEntity` — non-null (`@NotNull`, no `?`) — `programmeGroup.code: String` is non-null on the target entity ✓
- `createdByUsername: String?` — **nullable in Kotlin type** despite `@NotNull`
  annotation. `compareBy.thenBy` handles nulls via `compareValues` (null < non-null)
  deterministically, so this is still a valid tiebreak — but the follow-up
  audit should flag this Kotlin-vs-JPA nullability mismatch as a separate
  cleanliness item.

**`ReferralStatusHistoryEntity`** (verified from source, 2026-08-25):

- `createdAt: LocalDateTime` — non-null
- `createdBy: String` — non-null
- `referralStatusDescription: ReferralStatusDescriptionEntity` — non-null (`val`, no `?`).
  Its `description: String` field should be non-null — **verify in Step 1 by opening `ReferralStatusDescriptionEntity.kt`**; if `description` is nullable, use `referralStatusDescription.id?.toString()` as the tertiary tiebreak instead (accepting the "UUID as tiebreak" caveat only because it's tertiary after two non-null keys).

---

## Plan

### Step 0 — reproduce current tie behaviour (Part A only)

Not strictly required for Part B (telemetry symmetry, low risk), but for
Part A: write a throwaway integration test that seeds a referral with two
`programmeGroupMemberships` sharing the same `createdAt` (millisecond
truncation is easiest) and different `programmeGroup.code`. Run
`getCompletionDataForReferral` twice with clean JVM start — confirm which
membership wins is currently unstable (or at least is not documented). Delete
the throwaway test before opening the PR.

### Step 1 — verify `ReferralStatusDescriptionEntity.description` nullability

```bash
grep -nE "var description|val description" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ReferralStatusDescriptionEntity.kt
```

If non-null: use `.thenBy { referralStatusDescription.description }`. If
nullable: use `.thenBy { referralStatusDescription.id }` (UUID, tertiary
tiebreak — acceptable because primary + secondary keys are non-null).

### Step 2 — Part A implementation

1. Add `@OrderBy("createdAt DESC")` to
   `ReferralEntity.programmeGroupMemberships` (verify the `@OrderBy` import
   is already in the file — line 73's `statusHistories` uses it, so yes).
2. Replace the two `.maxByOrNull` calls in `ReferralStatusService` with
   `.sortedWith(...).firstOrNull()`. Extract the comparator into a local
   `val` if both sites share it in the same file (they do — same class).
3. Import cleanup: no changes expected (`ProgrammeGroupMembershipEntity` is
   already imported).

### Step 3 — Part B implementation

Mirror the existing `referralCohortHistories` fix in the same file (the
`sortedWith(...)` block, look above the current cohort telemetry property
in `TelemetryService.kt`). Two-line diff each for `referralStatus` and
`fromStatus`.

### Step 4 — tests

- **Part A** — extend `ReferralStatusServiceTest` (or add a small focused
  test class if the existing one is too crowded) with two tests:
  1. Two memberships with tied `createdAt` and different `programmeGroup.code` →
     `getCompletionDataForReferral` uses the higher-`code` membership (deterministic
     across JVM runs — assert by running the test twice via `@RepeatedTest(3)`).
  2. Same setup → `hasValidPostProgrammeReviewAttendance` picks the same
     membership as the getter above (proves both sites resolve identically).
- **Part B** — no unit test needed (telemetry side-effect, low value to
  mock `TelemetryClient` just for a tiebreak assertion). Add a KDoc comment
  above each `sortedWith` explaining the intent, matching the cohort fix's
  comment style.

### Step 5 — run full suite

`./gradlew ktlintCheck test` — should be green first try. If any existing
test now fails, it means that test was implicitly relying on an unstable
membership pick — investigate rather than paper over.

---

## Acceptance criteria (all must be true before opening the PR)

- [ ] `ReferralEntity.programmeGroupMemberships` has `@OrderBy("createdAt DESC")`.
- [ ] Both `ReferralStatusService.kt` sites use the same explicit
      `.sortedWith(...)` comparator (extracted to a local `val` in the file
      or a top-level `private val` if used across methods).
- [ ] `TelemetryService.kt` — `referralStatus` and `fromStatus` telemetry
      properties both have an explicit `.sortedWith(...)` block matching the
      shape of the `referralCohortHistories` fix already in the file.
- [ ] `@RepeatedTest(3)` tests demonstrate deterministic membership selection
      across JVM restarts (Part A).
- [ ] `./gradlew ktlintCheck test` — BUILD SUCCESSFUL, all tests pass.
- [ ] Grep sanity: `grep -rn "programmeGroupMemberships\.maxByOrNull" src/main/kotlin` returns 0.
- [ ] Grep sanity: `grep -rnE "statusHistories\?\.firstOrNull" src/main/kotlin/**/TelemetryService.kt` returns 0.
- [ ] PR body cites this doc and PR #880 as the reference pattern for the
      `sortedWith` idiom.

---

## Implementer prompt (fresh chat — self-contained)

You are implementing APG-2580 follow-up 6 — two thematically-linked
order-sensitive read hardening tasks:

1. **Part A** — Add `@OrderBy("createdAt DESC")` to
   `ReferralEntity.programmeGroupMemberships` and add explicit
   `.sortedWith(...)` tiebreak at the two `ReferralStatusService` consumer
   sites (functional — drives referral-completion event).
2. **Part B** — Mirror the `referralCohortHistories` fix (already on `main`
   from PR #880) onto `statusHistories` in `TelemetryService` for the
   `referralStatus` + `fromStatus` telemetry properties (symmetry).

Both use the same idiom: JPA-level `@OrderBy` where possible, plus in-Kotlin
`sortedWith(compareByDescending { createdAt }.thenBy { … }.thenBy { … }).firstOrNull()`
for deterministic tiebreak (matches audit correction #8).

### Prerequisites (verify first)

1. `git log origin/main --oneline | head -5` — confirm your local `main` is
   current.
2. Read this doc end-to-end.
3. Verify `ReferralStatusDescriptionEntity.description` nullability (Step 1
   above) — the choice of tertiary tiebreak in Part B depends on it.

### Setup

```bash
git fetch origin
git checkout main && git pull
git checkout -b APG-2580/membership-and-status-history-tiebreaks
```

### Plan to execute

Follow **Plan Steps 0 → 5** above verbatim.

### Acceptance criteria

See the numbered checklist above.

### Push and open PR

```bash
git push -u origin APG-2580/membership-and-status-history-tiebreaks
gh pr create --base main \
  --title "APG-2580 Add order tiebreaks to programmeGroupMemberships + statusHistories reads" \
  --body-file /tmp/pr-body.md
```

Draft the PR body from the "What / How / Why now / Verification / Scope note"
template used by PR #880. Split it into "Part A" and "Part B" subsections
so reviewers can hold them in mind separately.

### Report back

Post to the planning agent:

- SHAs of head + squash-merge if merged.
- Whether `ReferralStatusDescriptionEntity.description` was nullable (so the
  audit report can be updated with the final tiebreak choice).
- Any behavioural drift in existing tests (should be zero — flag if not).
- Whether any pre-existing test was silently relying on unstable membership
  order (Step 5 caveat).

