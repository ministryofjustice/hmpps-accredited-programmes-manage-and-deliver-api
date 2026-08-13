# Branch 2 (APG-2580): SAR – Replace Attendance Outcome Codes with Descriptions

**Branch name:** `APG-2580/attendance-codes-to-descriptions`  
**Base branch:** `APG-2580/remove-pii-and-duplicate-sections` (Branch 1 must be merged or this should be branched from it)  
**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT  
**Estimated effort:** 0.5 days  
**Next branch (depends on this):** `APG-2580/enum-display-names`

---

## What This Branch Does

The ticket requires replacing internal system codes with descriptive, human-readable values. The concrete example given in the ticket:

> "Attendance outcomes should display 'Attended' rather than codes such as 'ATTC'."

**Root cause:** The `SubjectAccessRequestSessionAttendanceNDeliusOutcome` DTO currently maps `code.name` → the raw enum string (e.g. `"ATTC"`, `"UAAB"`). The `SessionAttendanceNDeliusOutcomeEntity` already has a `description` field (e.g. `"Attended"`) in the database. We just need to expose it.

**The template currently renders:**
```html
<td>{{ optionalValue outcomeType.outcomeTypeCode }}</td>
```
This shows `ATTC`. After this change it should show `Attended`.

---

## Background – Existing Entity Structure

**`SessionAttendanceNDeliusOutcomeEntity`** (do NOT modify this file – it already has everything needed):

```
Path: src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/SessionAttendanceNDeliusOutcomeEntity.kt
```

```kotlin
@Entity
@Table(name = "session_attendance_ndelius_outcome")
class SessionAttendanceNDeliusOutcomeEntity(
  @Id
  @Column(name = "code", nullable = false, length = 5)
  @Enumerated(STRING)
  var code: SessionAttendanceNDeliusCode,         // e.g. ATTC enum value

  @Column(name = "description")
  var description: String? = null,                // e.g. "Attended" ← this is what we want

  @Column(name = "attendance")
  var attendance: Boolean? = null,

  @Column(name = "compliant", nullable = false)
  var compliant: Boolean,
)
```

The `description` field comes from the `session_attendance_ndelius_outcome` database table which is seeded with values like:
- `ATTC` → `"Attended"`
- `UAAB` → `"Did not attend"`
- etc.

Note: `ProgrammeGroupService.getAttendanceTextFromOutcome()` already has human-readable logic:
```kotlin
fun getAttendanceTextFromOutcome(attendanceOutcome: SessionAttendanceNDeliusOutcomeEntity?): String = when (attendanceOutcome?.code) {
  UAAB -> "Did not attend"
  null -> "To be confirmed"
  else -> attendanceOutcome.description!!
}
```
We follow the same pattern — use `description` where available.

---

## Files to MODIFY

### 1. `SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity

data class SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  val outcomeTypeCode: String,
  val attendance: Boolean?,
  val compliant: Boolean,
)

fun SessionAttendanceNDeliusOutcomeEntity.toApi() = SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  outcomeTypeCode = code.name,
  attendance = attendance,
  compliant = compliant,
)
```

**New content — rename `outcomeTypeCode` to `outcomeDescription` and map from `description` instead of `code.name`:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity

data class SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  val outcomeDescription: String?,
  val attendance: Boolean?,
  val compliant: Boolean,
)

fun SessionAttendanceNDeliusOutcomeEntity.toApi() = SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  outcomeDescription = description,
  attendance = attendance,
  compliant = compliant,
)
```

> **Note:** `description` is nullable (`String?`) on the entity, hence `outcomeDescription` is also nullable here. The template already uses `{{ optionalValue ... }}` which handles nulls gracefully.

---

### 2. `sar_template.mustache`

**Path:**
```
src/main/resources/sar_template.mustache
```

**Single change — update the attendance outcome cell to use `outcomeDescription` instead of `outcomeTypeCode`.**

Find this line (currently around line 182):
```html
        <td>{{ optionalValue outcomeType.outcomeTypeCode }}</td>
```

Replace with:
```html
        <td>{{ optionalValue outcomeType.outcomeDescription }}</td>
```

No other template changes in this branch.

---

## Test Fixture Regeneration

After making the code changes above, regenerate the test fixtures with:

```bash
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "*.SarContractIntegrationTest"
```

This generates two log files. Check the console output for the exact paths. They will be something like:
- `build/.../sar-api-response.json.log`
- `build/.../sar-generated-report.html.log`

Copy them over the test resources:
```bash
cp <path-to>/sar-api-response.json.log src/test/resources/sar/sar-api-response.json
cp <path-to>/sar-generated-report.html.log src/test/resources/sar/sar-expected-render-result.html
```

Verify the JSON fixture now contains `"outcomeDescription":"Attended"` (or whatever the seeded description is for `ATTC`) instead of `"outcomeTypeCode":"ATTC"`.

Finally confirm tests pass:
```bash
./gradlew test --tests "*.SarContractIntegrationTest"
```

---

## What to Check in the JSON Fixture

In `sar-api-response.json`, the attendance object currently looks like:
```json
"outcomeType":{"outcomeTypeCode":"ATTC","attendance":true,"compliant":true}
```

After this change it should look like:
```json
"outcomeType":{"outcomeDescription":"Attended","attendance":true,"compliant":true}
```

The integration test uses `SessionAttendanceNDeliusOutcomeEntityFactory` to seed test data. Check whether the factory sets a `description` on the outcome entity — if not, the description will be `null` and the fixture will show `"outcomeDescription":null`. This is acceptable for the test fixture (it still passes), but confirm with the team that the production database seed data has descriptions populated.

To check the factory:
```
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/factory/SessionAttendanceNDeliusOutcomeEntityFactory.kt
```

---

## Verification Checklist

- [ ] `SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt` — field renamed from `outcomeTypeCode` to `outcomeDescription`, mapped from `description` not `code.name`
- [ ] `sar_template.mustache` — line uses `outcomeType.outcomeDescription` not `outcomeType.outcomeTypeCode`
- [ ] No other files reference `outcomeTypeCode` in the SAR model (run a grep: `grep -r "outcomeTypeCode" src/main/`)
- [ ] `./gradlew test --tests "*.SarContractIntegrationTest"` passes
- [ ] Test fixtures updated
- [ ] JSON fixture shows `outcomeDescription` key

