# Branch 5 (APG-2580): SAR – Fix Set-typed collections collapsing to "No Data Held"

**Branch name:** `APG-2580/render-test-seed-attendance-row` *(legacy name — kept for tracker/PR continuity; actual scope is a DTO type fix, not a seed enrichment — see § "Notes on the earlier scoping")*
**Base branch:** `main` (after Branch 4 / PR #874 merges)
**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Estimated effort:** half a day
**This is the final branch for the ticket.**

---

## What this branch does

Fixes a pre-existing bug in the SAR template rendering that causes three whole sections to always show `<p>No Data Held</p>` regardless of whether the subject actually has data.

**Confirmed by planning-agent investigation on 2026-08-24 (see Correction #9 in the delivery tracker):**

| Field | JSON serialises correctly? | HTML renders correctly? | Kotlin DTO field type |
|-------|---------------------------|-------------------------|-----------------------|
| `programmeGroupMemberships` | Yes | No — always "No Data Held" | `MutableSet` |
| `referralCohortHistories` | Yes | No — always "No Data Held" | `MutableSet` |
| `referralLdcHistories` | Yes | No — always "No Data Held" | `MutableSet` |
| `statusHistories` | Yes | Yes | `MutableList` |
| `attendees` | Yes | Yes | `MutableList` |

**Root cause:** the SAR mustache template uses `{{#field.0}}` (list-index access at position 0) as its "is-non-empty" guard for these collections. Kotlin `MutableSet` does not expose a `[0]` / `get(0)` accessor, so JMustache evaluates `field.0` on a Set as falsy → the `{{^field.0}}<p>No Data Held</p>{{/field.0}}` fallback always fires, even when the Set has entries.

**Production impact:** Real Community SARs delivered to subjects have been showing "No Data Held" for these three sections since the SAR feature shipped (PR #644). The JSON endpoint response has always been correct; only the human-readable PDF/HTML report was affected.

The fix also serves the original Branch 5 goal: once `programmeGroupMemberships` renders correctly, the mustache changes from Branches 2 (`outcomeDescription`) and 4 (per-attendance `<div>`, `recordedBy` note field) become snapshot-covered by `sar-expected-render-result.html`.

---

## Files to MODIFY

Only **one production file** and (via regen) two test fixtures.

### 1. `SubjectAccessRequestReferral.kt`

**Path:** `src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt`

**Required changes — three DTO field types + three mapper lines, plus sort safeguards on two of them:**

- `programmeGroupMemberships`: type `MutableSet<...>` → `MutableList<...>`; mapper `.toMutableSet()` → `.toMutableList()`; add `.sortedWith(compareBy<ProgrammeGroupMembershipEntity> { it.createdAt }.thenBy { it.id })` before the `.map { it.toApi() }`.
- `referralLdcHistories`: same three changes, with sort direction `compareByDescending<ReferralLdcHistoryEntity> { it.createdAt }.thenBy { it.id }`.
- `referralCohortHistories`: type + `.toMutableSet()` → `.toMutableList()` only. **Do not touch the existing `.sortedWith(...)` — it already has the correct pattern from Branch 1.**
- Leave `statusHistories`, `messageHistories`, `attendees`, and everything else untouched.
- Add imports for `ProgrammeGroupMembershipEntity` and `ReferralLdcHistoryEntity` (or whatever the actual entity classes are called — verify against the file's existing imports).

### Sort direction rationale (Correction #8 pattern)

Once the field becomes a `List`, ordering matters — JPA loads Sets in arbitrary hash order, so converting to List without a sort makes order SQL/JVM-dependent (CI vs local can differ; same class of flake as `referralCohortHistories` on Branch 1).

Suggested directions (match the entity's `@OrderBy` if it disagrees):

| Field | Direction | Rationale |
|-------|-----------|-----------|
| `programmeGroupMemberships` | ASC by `createdAt` | Chronological allocation order reads naturally in the report |
| `referralLdcHistories` | DESC by `createdAt` | Most-recent-first matches `referralCohortHistories` |
| `referralCohortHistories` | DESC by `createdAt` | Already in place — do not change |

All three should have `.thenBy { it.id }` as tiebreaker.

---

## Files that regenerate (do NOT hand-edit)

- `src/test/resources/sar/sar-api-response.json` — will be regenerated. Expected changes: minimal. Sets and Lists both serialise as JSON arrays via Jackson, so shape is identical; only ordering may shift for `programmeGroupMemberships` and `referralLdcHistories` because the new `sortedWith` calls impose a stable order. `referralCohortHistories` was already sorted — no change expected there (still pinned to CI's order by Branch 3's `158cb481` commit).
- `src/test/resources/sar/sar-expected-render-result.html` — will be regenerated. Expected changes: the three sections now show real data instead of "No Data Held".

---

## Files to CHECK but likely leave alone

- `src/main/resources/sar_template.mustache` — no change. The `{{#field.0}}` idiom starts working correctly once the fields are `List`-typed.
- Any consumer of the SAR JSON — no change. JSON output shape is identical.
- `SubjectAccessRequestServiceTest.kt` — check assertions on the three affected fields (grep suggested below).

---

## Pre-flight greps (mandatory)

Run these BEFORE committing. Any hit outside `SubjectAccessRequestReferral.kt` and the two SAR fixture files should be reviewed:

```
grep -rn "programmeGroupMemberships: MutableSet" src/main/
grep -rn "referralLdcHistories: MutableSet" src/main/
grep -rn "referralCohortHistories: MutableSet" src/main/
grep -nE "programmeGroupMemberships|referralLdcHistories|referralCohortHistories" src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SubjectAccessRequestServiceTest.kt
grep -nE "programmeGroupMemberships|referralLdcHistories|referralCohortHistories" src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/HmppsSubjectAccessRequestControllerIntegrationTest.kt
grep -nE "programmeGroupMemberships\.0|referralLdcHistories\.0|referralCohortHistories\.0" src/main/resources/sar_template.mustache
```

For test-side hits: `.containsExactlyInAnyOrder` works on any `Iterable`, so it should be safe. `.hasSize` also safe. `.first()` / `.last()` return different elements depending on the underlying type — inspect any hits.

---

## Verification checklist

- [ ] Three DTO field types changed from `MutableSet<...>` to `MutableList<...>`.
- [ ] Three mapper `.toMutableSet()` calls changed to `.toMutableList()`.
- [ ] `sortedWith(...)` added for `programmeGroupMemberships` and `referralLdcHistories` (already present on `referralCohortHistories`).
- [ ] Imports for the two entity classes added if not already present.
- [ ] Pre-flight greps clean.
- [ ] `./gradlew ktlintCheck` passes.
- [ ] `./gradlew test --tests "*SubjectAccessRequestServiceTest*"` passes.
- [ ] `./scripts/local-scripts/regenerate-sar-snapshots.sh` completes without errors.
- [ ] `sar-expected-render-result.html` no longer has `<p>No Data Held</p>` immediately after `<h3>Programme Group Memberships</h3>`, `<h3>Cohort History</h3>`, or `<h3>LDC History</h3>`.
- [ ] `sar-expected-render-result.html` now shows Branch 2 output (`Attended - Complied`) and Branch 4 layout (`<div class="attendance-record">`, `<td>Outcome</td>`, `<td>Legitimate Absence</td>`, `<td>Recorded At</td>`).
- [ ] `sar-api-response.json` diff is minimal — expect only ordering shifts inside `programmeGroupMemberships` and `referralLdcHistories`.
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes (rerun once if the cohort-ordering flake fires — Correction #8).
- [ ] `./gradlew build` passes.

---

## What Branch 5 does NOT include

- No changes to the mustache template.
- No changes to entity classes.
- No CSS work.
- No fix for the `refreshPersonalDetailsForReferral` async race.
- No seed enrichment.
- No changes to the SAR JSON endpoint output shape.

---

## Notes on the earlier "seed enrichment" scoping (now obsolete)

Earlier planning framed Branch 5 as "add one attendance row to the render-test seed so the mustache changes from Branches 2 and 4 are exercised". Investigation on 2026-08-24 disproved this: the seed DOES produce attendance data (verified via `sar-api-response.json` on `main`), the JSON output confirms it, but the HTML output collapses it because of the Set-vs-List mustache-accessor bug documented above. **Seed enrichment is not required; a DTO type-change is.**

The branch name `APG-2580/render-test-seed-attendance-row` is kept for tracker continuity. When the implementer opens the PR, the title should reflect the actual scope: `APG-2580 Fix SAR sections silently collapsing to "No Data Held" (community)`.

