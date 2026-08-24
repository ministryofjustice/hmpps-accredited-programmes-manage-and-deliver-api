# APG-2580 Follow-up 1 — Kill the `sar-api-response.json` cohort-ordering pin (HIGH PRIORITY)

**Parent ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Suggested branch name:** `APG-2580/repin-sar-json-fixture`
**Base branch:** `main` (after PR #877 / Branch 5 merges)
**Priority:** **HIGH** — every APG-2580 branch since Branch 3 has been carrying a "known CI-pinned cohort ordering flake" as a permanent override (tracker override #6). It has now leaked into Branch 5's local `./gradlew build` as a deterministic failure. The pin is fragile and blocks confidence in the SAR contract test.
**Estimated effort:** half a day (fix + fixture regen + one green CI run to confirm)

---

## Verified facts (as of 2026-08-24, commit `50279ec4` on `main`)

The following are **grep-verified** against the current source, not assumed. Any implementer picking this up MUST re-verify these before touching code — the file line numbers may drift.

### F1. The pinned JSON currently contains three cohort entries

File: `src/test/resources/sar/sar-api-response.json` (single-line JSON).

```
"referralCohortHistories":[
  {"createdBy":"AUTH_USER", ..., "cohort":"General offence"},
  {"createdBy":"SYSTEM",    ..., "cohort":"Sexual offence"},
  {"createdBy":"AUTH_USER", ..., "cohort":"General offence"}
]
```

Two of the three share `createdBy = "AUTH_USER"` and `cohort = "General offence"`. Their `createdAt` values are stubbed to `"<DATE_TIME>"` at snapshot-scrub time (see the `<DATE_TIME>` replacer in `scripts/local-scripts/regenerate-sar-snapshots.sh` — implementer must open the script and confirm the exact regex).

### F2. The seed that produces these three entries

File: `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt` lines **163–176** (verify line numbers before editing).

```kotlin
val referral = referrals[0]
referral.referralCohortHistories.add(
  ReferralCohortHistoryFactory()
    .withReferral(referral)
    .withCohort(OffenceCohort.GENERAL_OFFENCE)
    .withCreatedBy("AUTH_USER")
    .produce(),
)
referralRepository.saveAndFlush(referral)

referral.referralCohortHistories.forEach {
  it.createdAt = if (it.createdBy == "SYSTEM" || it.createdBy == "Accredited Programmes automated update") fixedNow else fixedNow.minusMinutes(1)
}
referralRepository.saveAndFlush(referral)
```

**Observation:** the `forEach` normalises `createdAt` to only **two distinct values** — `fixedNow` (for SYSTEM/automated rows) and `fixedNow.minusMinutes(1)` (for AUTH_USER rows). Since there are **two AUTH_USER rows** in the final state, both end up with **exactly the same `createdAt`** (`fixedNow.minusMinutes(1)`).

### F3. The mapper's sort behaviour post-Branch 5

File: `src/main/kotlin/.../subjectAccessRequest/SubjectAccessRequestReferral.kt` lines 63–69 (post-Branch 5 comment additions).

```kotlin
referralCohortHistories = referralCohortHistories
  .sortedWith(compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }.thenBy { it.id })
  .map { it.toApi() }
  .toMutableList()
```

Tie-breaker is `.thenBy { it.id }` — where `id` is a **UUID assigned by the DB at insert time** (verify: `ReferralCohortHistoryEntity` `id` field annotation). UUIDs from Postgres `gen_random_uuid()` (or Hibernate's UUID generator, depending on config) are effectively random; their sort order is **not** deterministic across JVM runs, DB restarts, or CI-vs-local environments.

### F4. Why the pin has drifted

- The pinned JSON was captured on **CI** on a specific run (see tracker override #6, referencing Branch 3's commit `158cb481`).
- On that CI run, the two tied AUTH_USER "General offence" UUIDs happened to sort in a particular order.
- Any subsequent regen (local machine, different CI worker, DB restart, Hibernate version bump) can produce a different UUID order → JSON diff → test fails.
- Branch 5 local build confirmed this deterministically on Raby's machine (per PR #877 implementer report).

**Conclusion:** the pin is fundamentally unstable because the sort key `(createdAt, id)` does not induce a total ordering on the seed data. The `id` component varies between environments.

---

## Design: three viable fix strategies

The implementer must pick **exactly one** of these — do not combine. Trade-offs are listed. Recommendation is **Strategy A**.

### Strategy A (RECOMMENDED): natural attribute tie-break in the mapper

Change the mapper sort to use attributes that already exist on the entity and are stable across environments:

```kotlin
.sortedWith(
  compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }
    .thenBy { it.createdBy }
    .thenBy { it.cohort.name },
)
```

**Pros:**
- Zero seed changes; zero fixture changes are *required* (though a regen is needed to confirm ordering matches the pin — see verification below).
- Fully deterministic — no UUID, no clock, no environment coupling.
- Same pattern is safe to apply to `referralLdcHistories` (tiebreak on `hasLdc`, `createdBy`) and `programmeGroupMemberships` (tiebreak on `programmeGroup.id` which is a stable seed-side value, or `createdByUsername`).

**Cons:**
- Depends on the tuple `(createdAt, createdBy, cohort)` being unique across all realistic data. For the pinned seed, the two tied rows have `createdBy = "AUTH_USER"` and `cohort = "General offence"` — **still identical**. So Strategy A ALONE does not resolve the tie without also making one of these attributes distinct.
- **Therefore Strategy A must be combined with a seed change (see below) OR the current pin must be regenerated after switching to Strategy A + accepting that the two tied rows may swap (product-visible ordering of two identical-looking history entries is meaningless — the report reader sees them as duplicates anyway).**

### Strategy A' (RECOMMENDED VARIANT): Strategy A + seed disambiguation

Make the two AUTH_USER "General offence" cohort rows distinguishable. Simplest option — set distinct `createdAt` values in the seed's `forEach` normalisation:

```kotlin
// In SarContractIntegrationTest.kt setup block, replace the forEach at lines 173–175 with an
// indexed loop that stamps each AUTH_USER row a further minute apart:
referral.referralCohortHistories
  .filter { it.createdBy != "SYSTEM" && it.createdBy != "Accredited Programmes automated update" }
  .sortedBy { it.cohort.name }  // stable order for the assignment
  .forEachIndexed { index, entity ->
    entity.createdAt = fixedNow.minusMinutes(1L + index.toLong())
  }
referral.referralCohortHistories
  .filter { it.createdBy == "SYSTEM" || it.createdBy == "Accredited Programmes automated update" }
  .forEach { it.createdAt = fixedNow }
```

Combined with the mapper sort from Strategy A. Result: every cohort row has a unique `createdAt`, mapper sort is total-order deterministic, no UUID coupling.

**Pros:**
- Truly kills the flake at its root.
- Removes tracker override #6 permanently.

**Cons:**
- Requires a real fixture regen (`sar-api-response.json` cohort array reorders; possibly `sar-expected-render-result.html` too).
- Slightly more seed complexity.

### Strategy B: TestClock / manual clock injection

Wire a `@MockBean Clock` or `TestClock` into `ReferralService` so every `createdAt` timestamp is fully controlled by the test.

**Pros:**
- Solves the ordering problem AND every other timestamp-related non-determinism in SAR tests.
- Aligns with Spring best practice.

**Cons:**
- Large surface area — `ReferralService.createReferral()` uses `LocalDateTime.now()` implicitly via entity `@CreationTimestamp`/`@PrePersist`. Overriding this requires Hibernate-level clock control, not just constructor injection. **Confirmed via grep:** `grep -rn "LocalDateTime.now()\|Instant.now()\|@CreationTimestamp" src/main/kotlin --include="*.kt" | grep -i "referral"` — implementer must run this and count. If more than ~5 hits need refactor, park Strategy B as its own separate ticket.
- Out of scope for this follow-up.

### Strategy C: relax the JSON pin (jsonPathMatchers / ordered=false)

Change the assertion mechanism so cohort array ordering isn't asserted at all — use JSONassert with `strict=false` for that path, or a Hamcrest matcher.

**Pros:**
- No production or seed changes.

**Cons:**
- Weakens the contract test — regressions in ordering (which IS meaningful for LDC/status history) could sneak through.
- The pin exists precisely because the report was flaky and product wanted a canary; removing the canary is a policy decision.
- **Requires product sign-off** before pursuing.

---

## Chosen strategy for this branch

**Strategy A' (mapper tie-break by natural attributes + seed disambiguation).**

If the implementer discovers during pre-flight that the two tied rows have any other stable-and-distinct attribute (e.g. different `additionalDetails` — verify via grep on `ReferralCohortHistoryEntity` fields), they may fall back to pure Strategy A without a seed change. Document the decision in the PR body.

---

## Files to MODIFY

### 1. `SubjectAccessRequestReferral.kt`

**Path:** `src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt`

Replace the three `.thenBy { it.id }` tiebreakers with natural-attribute tiebreakers:

```kotlin
// programmeGroupMemberships (ASC by createdAt) — tiebreak on programmeGroup.id (stable seed UUID)
// then createdByUsername for absolute determinism.
.sortedWith(
  compareBy<ProgrammeGroupMembershipEntity> { it.createdAt }
    .thenBy { it.programmeGroup.id }
    .thenBy { it.createdByUsername },
)

// referralLdcHistories (DESC by createdAt) — tiebreak on hasLdc then createdBy.
.sortedWith(
  compareByDescending<ReferralLdcHistoryEntity> { it.createdAt }
    .thenBy { it.hasLdc }
    .thenBy { it.createdBy },
)

// referralCohortHistories (DESC by createdAt) — tiebreak on createdBy then cohort.name.
.sortedWith(
  compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }
    .thenBy { it.createdBy }
    .thenBy { it.cohort.name },
)
```

**Update the sort-rationale comments** (added on Branch 5) to reflect the new tiebreak keys and reference this follow-up doc.

**Verify before editing:**
- `grep -n "class ReferralCohortHistoryEntity\|class ReferralLdcHistoryEntity\|class ProgrammeGroupMembershipEntity" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/` — confirm the property names used above exist and are non-null. If any is nullable, wrap with `nullsLast()` / `nullsFirst()` as appropriate.

### 2. `SarContractIntegrationTest.kt`

**Path:** `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt`

Replace lines **~173–175** (verify) with the indexed-loop pattern shown in Strategy A' above. This guarantees every cohort row has a distinct `createdAt`.

**Do NOT** change the initial referral creation or the `.add(ReferralCohortHistoryFactory()...)` call. Only the timestamp-normalisation `forEach`.

### 3. `sar-api-response.json` (REGENERATE — do not hand-edit)

Run `scripts/local-scripts/regenerate-sar-snapshots.sh` after the code changes. Expected diff: cohort ordering may shift by one position; other sections unchanged.

**Commit the regen output** — this is the whole point of the follow-up. Delete tracker override #6 in the same PR.

### 4. `sar-expected-render-result.html` (REGENERATE — do not hand-edit)

Regenerated by the same script. Expected diff: at most a re-ordering of Cohort History rows.

---

## Pre-flight greps (MANDATORY — no guesswork)

Run all of these BEFORE writing code and record the counts in the PR body:

```bash
# 1. Confirm the seed pattern is still at the expected location
grep -n "referralCohortHistories.forEach" src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt

# 2. Confirm the mapper location and current tiebreakers
grep -n "thenBy { it.id }" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt
# Expect 3 hits.

# 3. Verify the three entities' field types (nullability, existence)
grep -n "createdBy\|cohort\|hasLdc\|createdByUsername\|programmeGroup" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ReferralCohortHistoryEntity.kt
grep -n "createdBy\|hasLdc" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ReferralLdcHistoryEntity.kt
grep -n "createdBy\|createdByUsername\|programmeGroup" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ProgrammeGroupMembershipEntity.kt

# 4. Any OTHER consumer that assumes the current UUID tiebreak order? (should be zero)
grep -rn "compareBy.*id\|thenBy.*id" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/

# 5. Confirm the regenerate script exists and know its flags
ls -la scripts/local-scripts/regenerate-sar-snapshots.sh
head -30 scripts/local-scripts/regenerate-sar-snapshots.sh

# 6. Snapshot the current tracker override #6 text — you'll delete it in the same PR
grep -A 3 "^6\.\|override #6" docs/branches/APG-2580-DELIVERY-TRACKER.md | head -20
```

**Stop and escalate** if:
- Grep #2 returns fewer than 3 hits — the mapper has drifted; re-plan.
- Grep #3 reveals nullable fields — the sort code above needs `nullsLast()`.
- Grep #4 returns unexpected hits — another DTO uses the same pattern and may need the same fix.

---

## Verification checklist

- [ ] Pre-flight greps run and counts recorded in PR body.
- [ ] Mapper sort changed for all three fields (grep expects 0 hits for `thenBy { it.id }` in this file after change).
- [ ] Seed `forEach` replaced with distinct-createdAt loop.
- [ ] `./gradlew ktlintCheck` — PASS.
- [ ] `./gradlew test --tests "*SubjectAccessRequestServiceTest*"` — PASS (still 2/2).
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` — PASS **locally** (this is the whole point — no more "known CI flake").
- [ ] `./gradlew build` — full green.
- [ ] `sar-api-response.json` regenerated via `scripts/local-scripts/regenerate-sar-snapshots.sh`.
- [ ] `sar-expected-render-result.html` regenerated by the same script.
- [ ] JSON diff inspected: cohort array reorders by at most one position; no other unexpected changes.
- [ ] HTML diff inspected: Cohort History section renders same 3 rows, possibly reordered.
- [ ] Tracker override #6 deleted from `docs/branches/APG-2580-DELIVERY-TRACKER.md`.
- [ ] PR body links this doc.
- [ ] PR body records grep counts and any deviations from Strategy A'.
- [ ] CI green on first push (this is a required stop-gate — if CI fails, do NOT re-pin the JSON to CI's order; instead diagnose why the sort is still non-deterministic).

---

## Anti-patterns (do NOT do these)

1. **Do not** re-pin the JSON to whatever CI produces if the test fails. That's exactly the trap override #6 has been carrying. If CI diverges from local after this fix, the sort is still non-deterministic and needs deeper investigation, not a re-pin.
2. **Do not** add `.thenBy { it.id }` back "just in case". UUID tiebreakers are the root cause.
3. **Do not** delete the pinned JSON. Contract tests need a real pin — this branch just makes the pin regeneration-stable.
4. **Do not** touch `sar_template.mustache` — Branch 5 already fixed the template-side bug.
5. **Do not** change `ReferralEntity`'s `MutableSet` fields — those are JPA-correct.

---

## Rollback plan

If the fix causes unexpected downstream failures (e.g. `HmppsSubjectAccessRequestControllerIntegrationTest` starts asserting a specific cohort order):

1. Revert only the mapper sort change (keep the seed disambiguation — it's independently useful).
2. Reintroduce `.thenBy { it.id }` **plus** the natural-attribute tiebreak: `.thenBy { it.createdBy }.thenBy { it.cohort.name }.thenBy { it.id }`. This gives determinism from the natural attrs and UUID as final backstop.
3. Regenerate fixtures.
4. Re-open a design conversation before removing the id tiebreaker again.

---

## Post-merge follow-ups

- Update `docs/branches/APG-2580-DELIVERY-TRACKER.md` "Corrections" list: mark override #6 as **resolved by this PR**, do not delete the history entry.
- Update `docs/branches/README.md` (if it exists on planning branch) with a link to this doc under "resolved follow-ups".
- Consider whether Strategy B (TestClock) should be scoped as its own hardening ticket — it would kill an entire class of timestamp flakes, not just this one.

---

## Implementer prompt (paste into a fresh Copilot / Cursor / Claude Code chat)

> **Ticket:** APG-2580 follow-up 1 — kill the sar-api-response.json cohort-ordering pin.
>
> **Base branch:** `main` (must be at or ahead of the merge commit for PR #877). Confirm with `git log --oneline -5 main` before starting; if PR #877 is not yet merged, STOP and tell the user.
>
> **Branch name:** `APG-2580/repin-sar-json-fixture`.
>
> **Read these docs first, in order, and confirm you have understood each:**
> 1. `docs/branches/APG-2580-followup-1-repin-sar-json-fixture.md` (this doc — the plan)
> 2. `docs/branches/APG-2580-DELIVERY-TRACKER.md` "Corrections" section, especially override #6 and correction #8 (the ordering flake history)
> 3. `docs/how-to/update-sar-tests.md` (fixture regeneration workflow)
>
> **Do the plan.** Follow Strategy A' from the doc. Run every pre-flight grep before touching code and paste the counts into your PR body. If any grep returns unexpected counts, STOP and ask the user before proceeding — do not guess.
>
> **Verification is non-negotiable:**
> - `./gradlew ktlintCheck` must PASS.
> - `./gradlew test --tests "*SubjectAccessRequestServiceTest*"` must PASS.
> - `./gradlew test --tests "*SarContractIntegrationTest*"` must PASS **locally** — no "known CI flake" carry-over allowed on this branch. If it fails locally, the fix is incomplete; do not push.
> - `./gradlew build` must be fully green before opening the PR.
>
> **Delete tracker override #6** in the same PR (see the "Verification checklist" and "Post-merge follow-ups" sections of the plan doc). This is a required deliverable — the whole point of the follow-up is to remove the override.
>
> **No guesswork rules:**
> - Do not assume field names, entity properties, or line numbers — verify with grep against the checked-out `main`.
> - Do not hand-edit either fixture file (`sar-api-response.json`, `sar-expected-render-result.html`). Only regenerate via the script.
> - Do not re-pin the JSON to whatever CI produces if CI disagrees with local — that is explicitly banned in the "Anti-patterns" section of the plan doc. If CI diverges, STOP and report back with the diff.
> - Do not change `ReferralEntity` — it stays `MutableSet` for JPA.
>
> **Report back to the planning agent with:**
> 1. Grep counts from all 6 pre-flight greps.
> 2. Chosen strategy (A' by default; note any deviation and why).
> 3. Local test results — verbatim gradle output for the three test targets above.
> 4. Fixture diff summary (line counts + which sections changed).
> 5. PR URL.
> 6. CI status on first push.
> 7. Any deviations, questions, or blockers.

