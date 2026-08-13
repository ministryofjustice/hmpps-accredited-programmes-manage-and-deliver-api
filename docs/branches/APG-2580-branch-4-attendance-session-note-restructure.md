# Branch 4 (APG-2580): SAR – Attendance and Session Note Restructure

**Branch name:** `APG-2580/attendance-session-note-restructure`  
**Base branch:** `APG-2580/enum-display-names` (Branch 3 must be merged or this should be branched from it)  
**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT  
**Estimated effort:** 1 day  
**This is the final branch for the ticket.**

---

## What This Branch Does

From the ticket:

> "We discussed the issues surrounding the 'Created By' field and agreed that these fields can be removed from the full report, provided the following information is grouped together as a single logical record:
> - Attendance record
> - Session outcome
> - Associated session note
>
> The section should retain the 'Recorded By' field, as this provides sufficient audit information."

**Two concerns addressed here:**

1. **Remove `Created By` + `Created At` from attendance records** — these fields (`createdBy` username, `createdAt` timestamp) are Spring audit fields that record who submitted the system record, not who delivered the session. They are confusing and not needed.

2. **Group each attendance + outcome + note as a single logical record** — currently the template renders ALL attendances in one flat table, then loops again for notes underneath. The redesign shows each attendance as its own block with its notes embedded directly beneath it.

3. **Fix session note `Created By`** — currently shows a username looked up via `getUserLastName` helper. The `SessionNotesHistoryEntity` already has a `createdByFullName` field (full name stored at write time) but it is NOT currently exposed in the SAR DTO. This branch adds it.

---

## Key Entity Facts (Do Not Modify These Files)

### `SessionAttendanceEntity`
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/SessionAttendanceEntity.kt
```
Fields relevant to this change:
- `recordedByFacilitator: FacilitatorEntity?` — the facilitator who recorded the attendance (linked entity with `.personName`)
- `recordedAt: LocalDateTime?` — when it was recorded
- `createdBy: String` — Spring `@CreatedBy` username (e.g. `"AUTH_USER"`) — **this is what we're REMOVING from the SAR**
- `createdAt: LocalDateTime` — system audit timestamp — **this is what we're REMOVING from the SAR**

### `SessionNotesHistoryEntity`
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/SessionNotesHistoryEntity.kt
```
Fields relevant to this change:
- `notes: String?` — the note text
- `createdBy: String` — Spring `@CreatedBy` username (e.g. `"AUTH_USER"`) — currently mapped in SAR DTO
- `createdAt: LocalDateTime` — timestamp
- `createdByFullName: String?` — full name stored at write time (e.g. `"Johnson"`) — **NOT YET in SAR DTO, add this**

---

## Files to MODIFY

### 1. `SubjectAccessRequestSessionAttendance.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestSessionAttendance.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionAttendance(
  val id: UUID?,
  val sessionId: UUID?,
  val groupMembershipId: UUID?,
  val outcomeType: SubjectAccessRequestSessionAttendanceNDeliusOutcome,
  val legitimateAbsence: String?,
  val noteHistory: MutableList<SubjectAccessRequestSessionNoteHistory>,
  val recordedByFacilitator: SubjectAccessRequestFacilitator?,
  val recordedAt: LocalDateTime?,
  val createdBy: String,
  val createdAt: LocalDateTime,
)

fun SessionAttendanceEntity.toApi() = SubjectAccessRequestSessionAttendance(
  id = id,
  sessionId = session.id,
  groupMembershipId = groupMembership.id,
  outcomeType = outcomeType.toApi(),
  legitimateAbsence = legitimateAbsence.toString(),
  noteHistory = notesHistory.map { it.toApi() }.toMutableList(),
  recordedByFacilitator = recordedByFacilitator?.toApi(),
  recordedAt = recordedAt,
  createdBy = createdBy,
  createdAt = createdAt,
)
```

**New content — remove `createdBy` and `createdAt` fields entirely:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionAttendance(
  val id: UUID?,
  val sessionId: UUID?,
  val groupMembershipId: UUID?,
  val outcomeType: SubjectAccessRequestSessionAttendanceNDeliusOutcome,
  val legitimateAbsence: String?,
  val noteHistory: MutableList<SubjectAccessRequestSessionNoteHistory>,
  val recordedByFacilitator: SubjectAccessRequestFacilitator?,
  val recordedAt: LocalDateTime?,
)

fun SessionAttendanceEntity.toApi() = SubjectAccessRequestSessionAttendance(
  id = id,
  sessionId = session.id,
  groupMembershipId = groupMembership.id,
  outcomeType = outcomeType.toApi(),
  legitimateAbsence = legitimateAbsence.toString(),
  noteHistory = notesHistory.map { it.toApi() }.toMutableList(),
  recordedByFacilitator = recordedByFacilitator?.toApi(),
  recordedAt = recordedAt,
)
```

---

### 2. `SubjectAccessRequestSessionNoteHistory.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestSessionNoteHistory.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionNoteHistory(
  val id: UUID?,
  val note: String?,
  val createdBy: String,
  val createdAt: LocalDateTime,
)

fun SessionNotesHistoryEntity.toApi() = SubjectAccessRequestSessionNoteHistory(
  id = id,
  note = notes,
  createdBy = createdBy,
  createdAt = createdAt,
)
```

**New content — replace `createdBy` (username) with `createdByFullName` (full name):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionNoteHistory(
  val id: UUID?,
  val note: String?,
  val recordedBy: String?,
  val createdAt: LocalDateTime,
)

fun SessionNotesHistoryEntity.toApi() = SubjectAccessRequestSessionNoteHistory(
  id = id,
  note = notes,
  recordedBy = createdByFullName,
  createdAt = createdAt,
)
```

> **Note:** `createdByFullName` is nullable on the entity (`String?`), so `recordedBy` is nullable here too. The template uses `{{ optionalValue recordedBy }}` which renders "No Data Held" for nulls.

---

### 3. `sar_template.mustache` — Attendance Section Restructure

**Path:**
```
src/main/resources/sar_template.mustache
```

**This is the main change in this branch.** The attendance section (currently lines 169–224 in the un-modified file, or the equivalent section starting at `<h4>Session Attendance</h4>` inside `{{#programmeGroupMemberships}}`) is completely restructured.

**Current structure of that section:**
```html
    <h4>Session Attendance</h4>
    {{#attendances.0}}
    <table class="data-table">
      <tr>
        <td class="data-column-20">Outcome</td>
        <td class="data-column-15">Legitimate Absence</td>
        <td class="data-column-20">Recorded By</td>
        <td class="data-column-20">Recorded At</td>
        <td class="data-column-15">Created By</td>
        <td class="data-column-10">Created At</td>
      </tr>
      {{#attendances}}
      <tr>
        <td>{{ optionalValue outcomeType.outcomeDescription }}</td>
        <td>
          {{#if (eq legitimateAbsence "null")}}
            No Data Held
          {{else}}
            {{#if (eq legitimateAbsence "")}}
              No Data Held
            {{else}}
              {{ convertBoolean legitimateAbsence }}
            {{/if}}
          {{/if}}
        </td>
        <td>{{ optionalValue recordedByFacilitator.personName }}</td>
        <td>{{ formatDate recordedAt }}</td>
        <td>{{ getUserLastName createdBy }}</td>
        <td>{{ formatDate createdAt }}</td>
      </tr>
      {{/attendances}}
    </table>

    {{#attendances}}
    {{#noteHistory.0}}
    <h5>Session Notes</h5>
    <table class="data-table">
      <tr>
        <td class="data-column-60">Notes</td>
        <td class="data-column-20">Created By</td>
        <td class="data-column-20">Created At</td>
      </tr>
      {{#noteHistory}}
      <tr>
        <td>{{ optionalValue note }}</td>
        <td>{{ getUserLastName createdBy }}</td>
        <td>{{ formatDate createdAt }}</td>
      </tr>
      {{/noteHistory}}
    </table>
    {{/noteHistory.0}}
    {{/attendances}}
    {{/attendances.0}}
    {{^attendances.0}}
    <p>No Data Held</p>
    {{/attendances.0}}
```

Note: in Branch 2, `outcomeType.outcomeTypeCode` was already changed to `outcomeType.outcomeDescription`. Branch 3 did not alter this section. This branch's template changes are relative to the post-Branch-3 template.

**New structure — replace the entire block above with this:**

```html
    <h4>Session Attendance</h4>
    {{#attendances.0}}
    {{#attendances}}
    <div class="attendance-record">
      <table class="summary-list">
        <tr><td>Outcome</td><td>{{ optionalValue outcomeType.outcomeDescription }}</td></tr>
        <tr>
          <td>Legitimate Absence</td>
          <td>
            {{#if (eq legitimateAbsence "null")}}
              No Data Held
            {{else}}
              {{#if (eq legitimateAbsence "")}}
                No Data Held
              {{else}}
                {{ convertBoolean legitimateAbsence }}
              {{/if}}
            {{/if}}
          </td>
        </tr>
        <tr><td>Recorded By</td><td>{{ optionalValue recordedByFacilitator.personName }}</td></tr>
        <tr><td>Recorded At</td><td>{{ formatDate recordedAt }}</td></tr>
      </table>
      {{#noteHistory.0}}
      <h5>Session Notes</h5>
      <table class="data-table">
        <tr>
          <td class="data-column-60">Notes</td>
          <td class="data-column-20">Recorded By</td>
          <td class="data-column-20">Recorded At</td>
        </tr>
        {{#noteHistory}}
        <tr>
          <td>{{ optionalValue note }}</td>
          <td>{{ optionalValue recordedBy }}</td>
          <td>{{ formatDate createdAt }}</td>
        </tr>
        {{/noteHistory}}
      </table>
      {{/noteHistory.0}}
    </div>
    {{/attendances}}
    {{/attendances.0}}
    {{^attendances.0}}
    <p>No Data Held</p>
    {{/attendances.0}}
```

**Key differences from current:**
1. Removed the outer flat `<table>` wrapping all attendances — replaced with `{{#attendances}}...{{/attendances}}` producing one `<div class="attendance-record">` per attendance
2. Each `<div>` contains a `summary-list` table (key-value rows) with: Outcome, Legitimate Absence, Recorded By, Recorded At — **no Created By, no Created At**
3. The notes `<table>` is now **inside** the attendance div — physically grouped with its parent attendance
4. The second `{{#attendances}}` loop is gone — notes appear inline
5. Notes column header changed from `Created By` to `Recorded By`
6. Notes `Recorded By` cell now uses `{{ optionalValue recordedBy }}` (the full name from `createdByFullName`) instead of `{{ getUserLastName createdBy }}`
7. Notes last column header changed from `Created At` to `Recorded At` (it still maps `createdAt` from the entity — the field name in the DTO is still `createdAt`)

---

## Locating the Section to Replace in the Template

The section to replace starts immediately after:
```html
    <h4>Session Attendance</h4>
```

And ends immediately before:
```html
  </div>
  {{/programmeGroupMemberships}}
```

Use these anchor lines to precisely locate the block. Everything between them (inclusive of the `<h4>` tag and up to but not including `</div>`) is replaced.

---

## Test Fixture Regeneration

After making the code changes above, regenerate the test fixtures:

```bash
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "*.SarContractIntegrationTest"
```

Check the console output for the paths to the generated `.log` files, then copy them:
```bash
cp <path-to>/sar-api-response.json.log src/test/resources/sar/sar-api-response.json
cp <path-to>/sar-generated-report.html.log src/test/resources/sar/sar-expected-render-result.html
```

Verify the JSON fixture no longer contains `"createdBy"` or `"createdAt"` inside the attendance objects.

Confirm tests pass without the flag:
```bash
./gradlew test --tests "*.SarContractIntegrationTest"
```

---

## What to Verify in the JSON Fixture

In `sar-api-response.json`, the attendance object currently looks like (after Branch 2):
```json
{
  "id": "<UUID>",
  "sessionId": "<UUID>",
  "groupMembershipId": "<UUID>",
  "outcomeType": {"outcomeDescription": "Attended - Complied", "attendance": true, "compliant": true},
  "legitimateAbsence": "null",
  "noteHistory": [{"id": "<UUID>", "note": "Notes for referral", "createdBy": "UNKNOWN_USER", "createdAt": "<DATE_TIME>"}],
  "recordedByFacilitator": null,
  "recordedAt": null,
  "createdBy": "UNKNOWN_USER",
  "createdAt": "<DATE_TIME>"
}
```

After this branch it should look like:
```json
{
  "id": "<UUID>",
  "sessionId": "<UUID>",
  "groupMembershipId": "<UUID>",
  "outcomeType": {"outcomeDescription": "Attended - Complied", "attendance": true, "compliant": true},
  "legitimateAbsence": "null",
  "noteHistory": [{"id": "<UUID>", "note": "Notes for referral", "recordedBy": null, "createdAt": "<DATE_TIME>"}],
  "recordedByFacilitator": null,
  "recordedAt": null
}
```

> Note: `recordedBy` will be `null` in the test fixture because the integration test creates `SessionNotesHistoryEntity` without setting `createdByFullName`. This is expected and correct — the test data is minimal. Production data will have `createdByFullName` set.

---

## Verification Checklist

- [ ] `SubjectAccessRequestSessionAttendance.kt` — `createdBy` and `createdAt` fields removed from data class and `toApi()`
- [ ] `SubjectAccessRequestSessionNoteHistory.kt` — `createdBy` replaced by `recordedBy: String?`, mapped from `createdByFullName`
- [ ] `sar_template.mustache` — attendance section restructured:
  - [ ] Flat attendance table replaced with per-attendance `<div>` blocks
  - [ ] Each div contains: Outcome, Legitimate Absence, Recorded By, Recorded At (no Created By, no Created At)
  - [ ] Session notes embedded inside each attendance div (not in a separate loop)
  - [ ] Notes column header is "Recorded By" not "Created By"
  - [ ] Notes `Recorded By` cell uses `{{ optionalValue recordedBy }}` not `{{ getUserLastName createdBy }}`
- [ ] `./gradlew build` compiles without errors
- [ ] `./gradlew test --tests "*.SarContractIntegrationTest"` passes
- [ ] JSON fixture has no `"createdBy"` or `"createdAt"` inside attendance objects
- [ ] JSON fixture has `"recordedBy"` key inside noteHistory objects

