# Branch 5 (APG-2580): SAR – Render-test snapshot coverage for attendance section

**Branch name:** `APG-2580/render-test-seed-attendance-row`
**Base branch:** `main` (after Branch 4 / PR #874 merges)
**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Estimated effort:** ½–1 day
**This is a test-infrastructure follow-up to Branches 2 and 4.**

---

## Why this branch exists

Branches 2 and 4 changed the SAR mustache template's attendance section:

- **Branch 2** — the attendance outcome `<td>` now emits `outcomeType.outcomeDescription` ("Attended - Complied") instead of `outcomeType.outcomeTypeCode` ("ATTC").
- **Branch 4** — the whole attendance table was replaced with per-attendance `<div class="attendance-record">` blocks containing a `summary-list` table plus a nested notes table; the notes table now shows `Recorded By` / `Recorded At` (mapped from `createdByFullName`) instead of `Created By` / `Created At`.

Both changes are covered by the JSON fixture (`sar-api-response.json`). **Neither is covered by the HTML fixture** (`sar-expected-render-result.html`) — as of the Branch 4 head commit, that file shows:

```html
<h3>Programme Group Memberships</h3>


<p>No Data Held</p>
```

...i.e. the whole Programme Group Memberships section is collapsed, so the attendance mustache lines Branch 2 + 4 changed are never emitted by the render test, and the fixture predates both changes (it still shows the pre-Branch-2 `<td>ATTC</td>` layout on stale branches; on `main` it just shows "No Data Held").

Branch 5 makes the HTML fixture actually exercise the attendance section, so future template changes are caught by snapshot rather than only by inspection.

---

## Investigation required (do this first)

**The JSON fixture and the HTML fixture disagree.** They should be produced from the same seed via the same `setupTestData()` call inside `SarContractIntegrationTest`, but:

- `sar-api-response.json` contains a real `programmeGroupMemberships[0].attendances[0]` with `outcomeType`, `noteHistory[0]`, etc.
- `sar-expected-render-result.html` collapses `programmeGroupMemberships` to `<p>No Data Held</p>`.

**Root cause is unknown at plan-drafting time.** The implementer's first task is to determine why. Candidates in order of likelihood:

1. **Hibernate LAZY fetch + transaction boundary.** The `SessionAttendanceEntity.notesHistory` and `SessionAttendanceEntity.session.attendances` collections are `@OneToMany(fetch = FetchType.LAZY)`. The JSON test serialises the SAR DTO through Jackson while the transaction is still open (the referral entity is loaded, `.toApi()` runs, JSON serialiser touches lazy collections which triggers a fetch). The render test additionally goes through `sarIntegrationTestHelper.renderServiceReport(data = dataResponse.content, template = templateResponse)` — if `dataResponse.content` is the **already-deserialised** SAR DTO (as it looks in the code) then lazy loading is a red herring here; but if `renderServiceReport` triggers a fresh round-trip somewhere, the collections could come back empty.
2. **`assertHtmlEquals` leniency.** The helper is provided by the shared `hmpps-kotlin-spring-boot-starter` library, not in-repo. It may normalise HTML aggressively enough that even a completely-different rendered output matches the "No Data Held" fixture. If so, the render test has been silently passing for a while regardless of the fixture. This is more of a shared-library concern than an APG-2580 concern, but the implementer should confirm the leniency model.
3. **Seed timing / transaction not committed.** `setupTestData()` calls `sessionRepository.saveAndFlush(session)` at the end after `session.attendances.add(attendance)`. If the enclosing transaction for the render test rolls back or is scoped differently to the JSON test, the attendance row may exist for the JSON test but not the render test.
4. **Mustache `{{#programmeGroupMemberships.0}}` short-circuit.** If the DTO passed to the render pipeline is somehow missing `programmeGroupMemberships` (e.g. a different DTO shape/serialisation) then the outer section fails and the inner `{{#attendances.0}}` never runs.

**Approach:** run the render test once with a temporary breakpoint / `println` at `renderServiceReport` (or at `dataResponse.content` right before rendering) and log the actual DTO passed to the template. If `programmeGroupMemberships` is empty there, the collapse is upstream of mustache; if non-empty, mustache/helper leniency is the culprit.

## Scope (what Branch 5 changes)

Once root cause is understood, apply the minimal fix. The three most likely shapes:

### Shape A — Seed / eager-load fix (in test code)

If the collapse is due to lazy loading / transaction boundary:
- **File:** `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt` (around line 289–298 — the attendance-seed block).
- **Change:** either (a) explicitly `session.attendances.add(...)` + `programmeGroupRepository.saveAndFlush(groupWithAllocation)` to make sure the group-membership → attendance link is committed, or (b) reload the entities after save so the `programmeGroupMemberships[0].attendances` collection is populated when the render test reads it.

### Shape B — DTO / mapper fix (in production code)

If the collapse is because `SubjectAccessRequestReferral.toApi()` returns an empty `programmeGroupMemberships` under render-test conditions:
- **File:** `src/main/kotlin/.../api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt` and/or the mappers it delegates to.
- **Change:** add explicit eager fetch (e.g. `programmeGroup.programmeGroupMemberships.size` inside the mapper to force initialisation) OR change repository query to fetch join.
- **Careful:** this touches production code — get product to re-confirm no perf regressions.

### Shape C — Helper leniency documented, no code change

If `assertHtmlEquals` is genuinely lenient enough that the current fixture passes despite the rendered output showing real attendance data:
- **File:** none in this repo.
- **Change:** update this doc + the tracker to record the leniency behaviour; regenerate `sar-expected-render-result.html` to reflect the rendered output; open a follow-up ticket against the shared library to tighten `assertHtmlEquals` (out of scope for APG-2580).

**Prefer Shape A.** Shape B is riskier (touches prod code). Shape C is the tell-me-nothing outcome — record the finding, still regenerate the fixture so future regressions are catchable.

---

## Files likely to be modified

| File | Likely change |
|------|---------------|
| `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt` | Seed / eager-load nudge (Shape A). Optional: enrich seed with `createdByFullName` on the note so `recordedBy` renders as a real name instead of "No Data Held". |
| `src/test/resources/sar/sar-expected-render-result.html` | Regenerated by `./scripts/local-scripts/regenerate-sar-snapshots.sh` |
| (Rarely) any SAR mapper on the production side | Only under Shape B — requires reviewer sign-off |

**No changes** to any DTO, entity, migration, or mustache template. Branch 5 is snapshot-coverage work.

---

## Acceptance criteria

- [ ] Investigation logged in PR description: which of Shape A / B / C applies and why.
- [ ] Root cause fix applied per chosen shape.
- [ ] `./scripts/local-scripts/regenerate-sar-snapshots.sh` completes.
- [ ] `sar-expected-render-result.html` now contains **all six** of the following (verified by grep):
  - `<div class="attendance-record">` — at least one occurrence
  - `<td>Attended - Complied</td>` — Branch 2 output
  - `<td>Outcome</td>` — Branch 4 summary-list header
  - `<td>Legitimate Absence</td>` — Branch 4 summary-list header
  - `<td>Recorded At</td>` — Branch 4 notes-table header (also new for the summary-list)
  - Either `<td>Notes for referral</td>` (the seeded note text) OR the notes header row `<td class="data-column-60">Notes</td>`
- [ ] `sar-expected-render-result.html` no longer contains `<p>No Data Held</p>` immediately below `<h3>Programme Group Memberships</h3>` (grep-verify with a 2-line context).
- [ ] `./gradlew ktlintCheck` passes.
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes without `SAR_GENERATE_ACTUAL`.
- [ ] `./gradlew build` passes.
- [ ] Cohort-ordering flake tolerance: expect at most 1 rerun per Correction #8 / Branch 3 override #4.

---

## What Branch 5 does NOT include

- No fix for the `refreshPersonalDetailsForReferral` async race (separate follow-up ticket).
- No changes to production seed / migrations.
- No enrichment of other under-covered SAR sections (e.g. delivery-location extras). Narrow scope to Programme Group Memberships → Session Attendance only.
- No CSS/styling work on the new `attendance-record` div (retracted 2026-08-24 — unstyled semantic classes are the project convention; there is no CSS anywhere in the repo).
- No changes to the `SubjectAccessRequestServiceTest` unit test — that already asserts on the JSON side.

---

## Ordering safety for notes (Branch 4 review finding)

`SessionAttendanceEntity.notesHistory` is a `MutableList<SessionNotesHistoryEntity>` with `@OrderBy("createdAt DESC")` at the entity level. Because it's a `List` (not a `Set`), Hibernate preserves the SQL `ORDER BY` result — so today's single-note fixture is safe.

**If your seed enrichment adds >1 note per attendance,** you MUST do one of the following to avoid the same class of flake as `referralCohortHistories` (Correction #8):

- **Preferred:** stagger `createdAt` by at least 1 second between notes so ordering is unambiguous. Example:
  ```kotlin
  val note1 = SessionNotesHistoryEntity(attendance = this, notes = "First note",
    createdAt = LocalDateTime.of(2026, 5, 1, 10, 0, 0))
  val note2 = SessionNotesHistoryEntity(attendance = this, notes = "Second note",
    createdAt = LocalDateTime.of(2026, 5, 2, 10, 0, 0))
  ```
- **Fallback:** apply a deterministic secondary sort in `SubjectAccessRequestSessionAttendance.toApi()`:
  ```kotlin
  noteHistory = notesHistory
    .sortedWith(compareByDescending<SessionNotesHistoryEntity> { it.createdAt }.thenBy { it.id })
    .map { it.toApi() }
    .toMutableList()
  ```
  Follows the Branch 1 pattern in `SubjectAccessRequestReferral.toApi()`.

If your seed adds only 1 note (recommended minimal scope — the current seed already does this), no action is needed. Keeping to 1 note per attendance means Branch 5 stays purely test-side.

---

## Verification checklist

- [ ] Investigation write-up in PR description (Shape A/B/C + evidence).
- [ ] Root cause fix applied.
- [ ] All six grep markers present in the regenerated HTML fixture (see acceptance criteria).
- [ ] `<p>No Data Held</p>` no longer directly follows `<h3>Programme Group Memberships</h3>`.
- [ ] JSON fixture (`sar-api-response.json`) unchanged (no drift) — Branch 5 is HTML-only.
- [ ] `./gradlew ktlintCheck` passes.
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes without `SAR_GENERATE_ACTUAL`.
- [ ] `./gradlew build` passes.
- [ ] `notesHistory` ordering safeguard applied only if >1 note per attendance was added; otherwise noted "N/A — single note".

