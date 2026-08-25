# APG-2580 Follow-up 5 — Consolidate attendance-latest reads through `latestByCreatedAt()`

**Status:** Not started
**Priority:** Low–Medium (functional in two sites, cosmetic in three)
**Blocked by:** PR #880 merged to `main` (helper must exist)
**Estimated size:** ~½ day
**Branch to create:** `APG-2580/attendance-latest-consolidation`

---

## Motivation

PR #880 shipped `Iterable<SessionAttendanceEntity>.latestByCreatedAt()` — a
consolidated helper with deterministic natural-attribute tiebreaks
(`createdAt` → `createdBy` → `outcomeType.code.name`) — and routed the two
UUID-lottery sites through it.

The follow-up-4 audit surfaced **5 additional call-sites** on the same
`MutableSet<SessionAttendanceEntity>` collections that read "the latest
attendance" using a bare `.maxByOrNull { it.createdAt }` (no tiebreak) or a
weak tiebreak. They are not UUID-lottery (so were out of scope for PR #880),
but they can still return different rows on tied `createdAt` — either
input-order-dependent (Kotlin `maxByOrNull` returns the first max) or
result-set-order-dependent (Hibernate returns whatever the DB gives).

Route the 5 sites through the helper (or a helper variant for site #4) so
future audits find one selector, not six.

---

## Scope — 5 call-sites (all verified against source, 2026-08-25)

All sites operate on `MutableSet<SessionAttendanceEntity>` reachable from
either `SessionEntity.attendances` or `ProgrammeGroupMembershipEntity.attendances`.
Neither Set has `@OrderBy`, so tiebreak is entirely in-Kotlin.

| # | Location | Current code | Refactor |
|---|---|---|---|
| 1 | `service/ReferralService.kt:688` (inside `flatMap` — attendance-history page) | `membership.attendances.filter { it.session.id == session.id }.maxByOrNull { it.createdAt }` | `membership.attendances.filter { it.session.id == session.id }.latestByCreatedAt()` |
| 2 | `service/SessionService.kt:461–467` (`saveSessionAttendance` — write-path dup-detect) | `attendances.maxWithOrNull(compareBy<SessionAttendanceEntity> { it.createdAt }.thenBy { it.recordedAt })` | `attendances.latestByCreatedAt()` — drops the `recordedAt` tiebreak (which was already weak because `recordedAt` is nullable), replaces it with the helper's `createdBy` + `outcomeType.code.name` tiebreaks which are non-null. **Semantic check:** the value is only used to compare notes with the incoming payload (dup-detect); tied rows have identical `notesHistory` in practice, so any tied winner is behaviourally equivalent for the caller. |
| 3 | `service/SessionService.kt:614–618` (`getSessionNotesFor…` — outcome text for notes page) | `session.attendances.filter { it.groupMembership.referralId == referralId }.maxByOrNull { it.createdAt }?.outcomeType` | `session.attendances.filter { it.groupMembership.referralId == referralId }.latestByCreatedAt()?.outcomeType` |
| 4 | `entity/NDeliusAppointmentEntity.kt:44` (`currentAttendance()` — used by `currentOutcomeCode()` and `currentSessionNotes()`) | `session.attendances.filter { it.groupMembership.referral.id == referral.id }.maxByOrNull { it.recordedAt ?: it.createdAt }` | **Different selector — do NOT use `latestByCreatedAt()` directly.** Options: (A) local `maxWithOrNull` with the `recordedAt ?: createdAt` selector plus the helper's tiebreak keys, or (B) add a sibling helper `latestByRecordedAtOrCreatedAt()` in `SessionAttendanceEntity.kt`. **Recommend (B)** — keeps all "latest" comparators in one file so the audit trail is one grep. |
| 5 | `api/model/sessionNotes/SessionNotes.kt:104–106` (`SessionNotes.from` — session-notes DTO builder) | `session.attendances.filter { it.groupMembership.referralId == referralId }.maxByOrNull { it.createdAt }` | `session.attendances.filter { it.groupMembership.referralId == referralId }.latestByCreatedAt()` |

### Tiebreak-key nullability (already verified in PR #880)

- `SessionAttendanceEntity.createdAt: LocalDateTime` — non-null (default)
- `SessionAttendanceEntity.createdBy: String` — non-null (`@CreatedBy`)
- `SessionAttendanceEntity.outcomeType: SessionAttendanceNDeliusOutcomeEntity` — non-null
- `SessionAttendanceEntity.recordedAt: LocalDateTime?` — **nullable** (site 4 must keep the `recordedAt ?: createdAt` selector)

---

## Plan

### Step 0 — reproduce current behaviour (mandatory)

Before editing any call-site, confirm the helper works via a smoke test on
each intended replacement. Concretely, for each of sites 1, 3, 5: write a
throwaway unit test that seeds two attendance rows with the same `createdAt`
but different `createdBy`; run the current code and the helper-based code
side-by-side; confirm the helper's chosen row equals the highest-`createdBy`
row. This grounds every claim in the report against real observed behaviour.
Delete these throwaway tests before opening the PR — the permanent contract
is already pinned by `SessionAttendanceEntityTest` on `main`.

### Step 1 — sites 1, 3, 5 (trivial routes)

Direct replacement — `.maxByOrNull { it.createdAt }` → `.latestByCreatedAt()`.
Add the `latestByCreatedAt` import to each file. No other logic changes.

### Step 2 — site 2 (SessionService.saveSessionAttendance)

Replace the whole `attendances.maxWithOrNull(compareBy…thenBy(recordedAt))`
block with `attendances.latestByCreatedAt()`. Semantic-equivalence rationale
(spelled out for the PR body): the current tiebreak was `recordedAt`
(nullable) which made ties resolve on input order when both `recordedAt`
were null; the helper's tiebreak is `createdBy` + `outcomeType.code.name`
(both non-null) which resolves ties deterministically. The value is only
consumed to compare notes with the incoming payload, so any tied winner is
behaviourally equivalent — but the helper makes the outcome stable across
JVM runs.

### Step 3 — site 4 (NDeliusAppointmentEntity.currentAttendance)

Add a second helper next to `latestByCreatedAt()` in
`entity/SessionAttendanceEntity.kt`:

```kotlin
/**
 * As [latestByCreatedAt] but ranks by `recordedAt` first (falling back to
 * `createdAt` when `recordedAt` is null). Used by
 * [NDeliusAppointmentEntity.currentAttendance] which wants the "when the
 * facilitator submitted" timestamp, not the row-creation timestamp.
 */
fun Iterable<SessionAttendanceEntity>.latestByRecordedAtOrCreatedAt(): SessionAttendanceEntity? =
  maxWithOrNull(
    compareBy<SessionAttendanceEntity> { it.recordedAt ?: it.createdAt }
      .thenBy { it.createdAt }
      .thenBy { it.createdBy }
      .thenBy { it.outcomeType.code.name },
  )
```

Then route `NDeliusAppointmentEntity.currentAttendance()` through it.

### Step 4 — tests

Add tests to `SessionAttendanceEntityTest` for the new helper:

1. Empty iterable → null.
2. Prefers `recordedAt` over `createdAt` when both present.
3. Falls back to `createdAt` when `recordedAt` is null.
4. Ties on the effective key resolve on `createdAt` then `createdBy` then
   `outcomeType.code.name` (input-order-independent).

Total new tests: **4 in the same test class**. No new integration tests
needed — the helper is already unit-covered; the 5 call-sites are covered by
existing integration tests (verify via `grep` for each entry-point).

### Step 5 — verify no behavioural drift in existing integration tests

Run `./gradlew test` full-suite. All should pass first attempt because the
helper's tie behaviour is strictly more specific than any of the replaced
selectors — untied rows produce the identical result, tied rows now produce
a stable result rather than an unstable one.

---

## Acceptance criteria (all must be true before opening the PR)

- [ ] All 5 call-sites re-route through `latestByCreatedAt()` (or
      `latestByRecordedAtOrCreatedAt()` for site 4). Verified by grep:
      `grep -rn "attendances\.(filter.*)?\.maxByOrNull" src/main/kotlin` returns 0 hits.
- [ ] Grep confirms no bare `.thenBy { it.id }` on `SessionAttendanceEntity`
      anywhere in `src/main`: `grep -rn "SessionAttendanceEntity.*thenBy.*id" src/main/kotlin` returns 0.
- [ ] New helper `latestByRecordedAtOrCreatedAt()` sits next to
      `latestByCreatedAt()` in `entity/SessionAttendanceEntity.kt` with a
      KDoc that explains why the selector differs.
- [ ] `SessionAttendanceEntityTest` has 4 additional tests covering the new
      helper.
- [ ] `./gradlew ktlintCheck test` — BUILD SUCCESSFUL, all tests pass first
      attempt.
- [ ] No new tests failing that weren't failing before (baseline the tree
      before starting).
- [ ] Import cleanup: any file where the last remaining use of
      `SessionAttendanceEntity` disappears has its import removed.
- [ ] PR body cites this doc and PR #880 as the reference pattern.

---

## Implementer prompt (fresh chat — self-contained)

You are implementing APG-2580 follow-up 5 — consolidate 5 attendance-latest
reads through the `latestByCreatedAt()` helper (shipped by PR #880 on
`main`) plus one new sibling helper `latestByRecordedAtOrCreatedAt()`.

### Prerequisites (verify first, do NOT skip)

1. `git log origin/main --oneline | grep -i "entity Set attendance"` — confirm PR
   #880's squash-merge commit is on `main`. If not, **stop and report back** —
   this ticket depends on that PR merging first.
2. `grep -n "fun Iterable<SessionAttendanceEntity>.latestByCreatedAt" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/SessionAttendanceEntity.kt` — confirm the helper exists on `main`.
3. Read this doc end-to-end; read PR #880's helper KDoc + `SessionAttendanceEntityTest`.

### Setup

```bash
git fetch origin
git checkout main && git pull
git checkout -b APG-2580/attendance-latest-consolidation
```

### Plan to execute

Follow **Plan Steps 0 → 5** above verbatim. Do Step 0 (reproduce) first —
do not skip.

For Step 4 tests, model each new test on the existing
`SessionAttendanceEntityTest` factory pattern
(`SessionAttendanceEntityFactory().withCreatedAt(…).withRecordedAt(…).produce()`).
Verify the factory has a `withRecordedAt` builder before using it; if not,
add one (single-line addition to the factory).

### Acceptance criteria

See the numbered checklist above.

### Push and open PR

```bash
git push -u origin APG-2580/attendance-latest-consolidation
gh pr create --base main \
  --title "APG-2580 Consolidate attendance-latest reads through latestByCreatedAt()" \
  --body-file /tmp/pr-body.md
```

Draft the PR body from the "What / How / Why now / Verification / Scope note" template used by PR #880.

### Report back

Post to the planning agent:

- SHAs of head + squash-merge if merged
- Any deviations from the plan
- Any behavioural drift observed in integration tests (should be zero — flag if not)
- Anything the audit report should be updated to reflect

---

## What this does NOT cover (kept for follow-up 6)

- `ReferralStatusService.kt:113,157` — `programmeGroupMemberships.maxByOrNull` (different collection, JPA-level fix better).
- `TelemetryService.statusHistories` for `referralStatus` + `fromStatus` (different entity family, `MutableList` not `MutableSet`, in-Kotlin `sortedWith` fix same shape as the cohort fix already shipped).

Both are in follow-up-6, `APG-2580-followup-6-membership-and-status-history-tiebreaks.md`.

