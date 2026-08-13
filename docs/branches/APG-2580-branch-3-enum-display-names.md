# Branch 3 (APG-2580): SAR – Enum Display Names for System Codes

**Branch name:** `APG-2580/enum-display-names`  
**Base branch:** `APG-2580/attendance-codes-to-descriptions` (Branch 2 must be merged or this should be branched from it)  
**Ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT  
**Estimated effort:** 0.5 days  
**Next branch (depends on this):** `APG-2580/attendance-session-note-restructure`

---

## What This Branch Does

The ticket says:

> "Reports should display user-friendly text rather than internal system codes or IDs. For example:
> - Programme name should display 'Building Choices' rather than the programme code.
> - Attendance outcomes should display 'Attended' rather than codes such as 'ATTC'.
> - Status fields should display clear, readable values rather than system-generated codes."

Branch 2 handled the attendance outcome code. Programme name already works. This branch handles all the **other** raw enum codes still leaking into the SAR report/JSON:

| Field in template | Currently renders | Should render |
|-------------------|-------------------|---------------|
| `interventionType` | `ACP` | `Accredited Programme` |
| `setting` | `COMMUNITY` | `Community` |
| `sourcedFrom` | `LICENCE_CONDITION` | `Licence Condition` |
| `session.moduleSessionTemplate.pathway` | `MODERATE_INTENSITY` | `Moderate Intensity` |
| `session.moduleSessionTemplate.sessionType` | `GROUP` | `Group` |
| `slotName` | `DAYTIME` | `Daytime` |

The referral **status history** (`referralStatusDescription.description`) already renders as `Scheduled`, `Awaiting allocation`, `Awaiting assessment` etc. from the DB, so no change needed there. Same for `cohort` (already renders as `General offence`, `Sexual offence`). LDC values are booleans and handled by `convertBoolean`.

---

## Approach

For each enum without a display name, add a `displayName: String` constructor property. For enums that already have display accessors (`SessionType.value`, `SlotName.displayName`), just change the SAR mapper to use them.

---

## Files to MODIFY

### 1. `InterventionType.kt` — add display names

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/type/InterventionType.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class InterventionType {
  SI,
  ACP,
  CRS,
  TOOLKITS,
}
```

**New content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class InterventionType(val displayName: String) {
  SI("Structured Intervention"),
  ACP("Accredited Programme"),
  CRS("Commissioned Rehabilitative Service"),
  TOOLKITS("Toolkits"),
}
```

> **⚠️ Confirm display names with product/service owner before merging.** These are best-guess expansions of the acronyms based on domain context. If unsure use `"Accredited Programme (ACP)"` style so the code is still visible.

---

### 2. `SettingType.kt` — add display names

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/type/SettingType.kt
```

**Current content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class SettingType {
  COMMUNITY,
  CUSTODY,
  REMAND,
  PRE_RELEASE,
}
```

**New content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class SettingType(val displayName: String) {
  COMMUNITY("Community"),
  CUSTODY("Custody"),
  REMAND("Remand"),
  PRE_RELEASE("Pre-release"),
}
```

---

### 3. `Pathway.kt` — add display names

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/type/Pathway.kt
```

**Current content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class Pathway {
  MODERATE_INTENSITY,
  HIGH_INTENSITY,
}
```

**New content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class Pathway(val displayName: String) {
  MODERATE_INTENSITY("Moderate Intensity"),
  HIGH_INTENSITY("High Intensity"),
}
```

---

### 4. `ReferralEntity.kt` — add display names on `ReferralEntitySourcedFrom` enum

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ReferralEntity.kt
```

The enum is at the bottom of `ReferralEntity.kt` (lines 170–173).

**Current content:**
```kotlin
enum class ReferralEntitySourcedFrom {
  REQUIREMENT,
  LICENCE_CONDITION,
}
```

**New content:**
```kotlin
enum class ReferralEntitySourcedFrom(val displayName: String) {
  REQUIREMENT("Requirement"),
  LICENCE_CONDITION("Licence Condition"),
}
```

---

### 5. `SubjectAccessRequestReferral.kt` — update mapper to use display names

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt
```

**Current mapper (assuming Branch 1 already merged — `crn`/`personName`/`dateOfBirth` already removed):**
```kotlin
fun ReferralEntity.toApi(
  messageHistoryEntities: List<MessageHistoryEntity>,
  attendeeEntities: List<AttendeeEntity>,
  availabilityEntity: AvailabilityEntity?,
) = SubjectAccessRequestReferral(
  id = id,
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  createdAt = createdAt,
  interventionName = interventionName,
  interventionType = interventionType.name,
  setting = setting.name,
  sourcedFrom = sourcedFrom?.name,
  // ...existing code...
)
```

**Change these 3 lines only:**
```kotlin
  interventionType = interventionType.displayName,
  setting = setting.displayName,
  sourcedFrom = sourcedFrom?.displayName,
```

Leave every other field untouched.

---

### 6. `SubjectAccessRequestSession.kt` — update mapper to use display names for pathway + sessionType

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestSession.kt
```

**Current mapper (lines 31–49):**
```kotlin
fun SessionEntity.toApi() = SubjectAccessRequestSession(
  createdByUsername = createdByUsername,
  endsAt = endsAt,
  locationName = locationName,
  startsAt = startsAt,
  module = SubjectAccessRequestModule(
    name = moduleSessionTemplate.module.name,
  ),
  moduleSessionTemplate = SubjectAccessRequestModuleSessionTemplate(
    description = moduleSessionTemplate.description,
    durationMinutes = moduleSessionTemplate.durationMinutes,
    moduleId = moduleSessionTemplate.module.id,
    name = moduleSessionTemplate.name,
    pathway = moduleSessionTemplate.pathway.name,
    sessionNumber = moduleSessionTemplate.sessionNumber,
    sessionType = moduleSessionTemplate.sessionType.name,
  ),
  sessionFacilitators = sessionFacilitators.map { it.toApi() },
)
```

**Change these 2 lines only:**
```kotlin
    pathway = moduleSessionTemplate.pathway.displayName,
    sessionType = moduleSessionTemplate.sessionType.value,
```

> Note: `SessionType` already has a `.value` accessor (`GROUP("Group")`, `ONE_TO_ONE("Individual")`) — use `.value`. `Pathway` uses the new `.displayName` we added above.

---

### 7. `SubjectAccessRequestAvailabilitySlot.kt` — use SlotName's existing display name

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestAvailabilitySlot.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity

data class SubjectAccessRequestAvailabilitySlot(
  val dayOfWeek: String,
  val slotName: String,
)

fun AvailabilitySlotEntity.toApi() = SubjectAccessRequestAvailabilitySlot(
  dayOfWeek = dayOfWeek.name,
  slotName = slotName.name,
)
```

**New content — capitalise the day and use `SlotName.displayName`:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity
import java.time.format.TextStyle
import java.util.Locale

data class SubjectAccessRequestAvailabilitySlot(
  val dayOfWeek: String,
  val slotName: String,
)

fun AvailabilitySlotEntity.toApi() = SubjectAccessRequestAvailabilitySlot(
  dayOfWeek = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.UK),
  slotName = slotName.displayName.replaceFirstChar { it.uppercase() },
)
```

> `SlotName.displayName` values are `"daytime"` / `"evening"` (lowercase). We capitalise for the report. Alternatively, edit the enum to store capitalised versions — do whichever aligns with existing style.

---

## Files NOT Modified

No template changes needed — the template already renders these fields via `{{ optionalValue ... }}`. The change is purely in the DTO mapping.

---

## Impact on Other Consumers

The `InterventionType`, `SettingType`, `Pathway`, `ReferralEntitySourcedFrom`, `SessionType`, `SlotName` enums are used in many places across the codebase (services, controllers, other DTOs). Adding a constructor parameter to a Kotlin enum is a **non-breaking change** — the enum name (`.name`) still works everywhere. Only the SAR mappers newly reference `.displayName`.

Run a full build to be safe:
```bash
./gradlew build
```

If anywhere in the codebase uses `enumValueOf<InterventionType>("ACP")` or `InterventionType.valueOf("ACP")`, those continue to work — the `.name` is unchanged.

---

## Test Fixture Regeneration

```bash
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "*.SarContractIntegrationTest"
```

Copy log outputs over fixtures then verify:
```bash
./gradlew test --tests "*.SarContractIntegrationTest"
```

---

## What to Verify in the JSON Fixture

Before this branch:
```json
"interventionType": "ACP",
"setting": "COMMUNITY",
"sourcedFrom": "LICENCE_CONDITION",
// ...
"pathway": "MODERATE_INTENSITY",
"sessionType": "GROUP",
// ...
"slots": [{"dayOfWeek": "MONDAY", "slotName": "DAYTIME"}]
```

After this branch:
```json
"interventionType": "Accredited Programme",
"setting": "Community",
"sourcedFrom": "Licence Condition",
// ...
"pathway": "Moderate Intensity",
"sessionType": "Group",
// ...
"slots": [{"dayOfWeek": "Monday", "slotName": "Daytime"}]
```

---

## Verification Checklist

- [ ] `InterventionType`, `SettingType`, `Pathway`, `ReferralEntitySourcedFrom` all have `displayName` constructor property
- [ ] `SubjectAccessRequestReferral.toApi()` uses `.displayName` for `interventionType`, `setting`, `sourcedFrom`
- [ ] `SubjectAccessRequestSession.toApi()` uses `.displayName` for `pathway` and `.value` for `sessionType`
- [ ] `SubjectAccessRequestAvailabilitySlot.toApi()` uses full day name + capitalised slot display name
- [ ] `./gradlew build` succeeds — no downstream compilation errors
- [ ] `./gradlew test --tests "*.SarContractIntegrationTest"` passes
- [ ] JSON fixture shows human-readable strings for all 6 fields listed above

