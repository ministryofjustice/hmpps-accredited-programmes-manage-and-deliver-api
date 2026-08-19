# Branch 1: SAR – Remove PII and Duplicate Sections

**Branch name:** `sar/remove-pii-and-duplicate-sections`  
**Base branch:** `main`  
**Ticket:** TECH: Update SAR endpoint and report for Community following UAT  
**Estimated effort:** 1 day  
**Next branch (depends on this):** `sar/attendance-codes-to-descriptions`

---

## What This Branch Does

Two requirements from the ticket:

1. **Remove unnecessary personal data** – Name, CRN, and Date of Birth must not appear in the SAR report.
2. **Remove Waitlist / Referral Case List sections** – These sections are confirmed duplicates of data already shown in the main Referral section. After PII is stripped, the only remaining fields (`sex`, `sentenceEndDate`, `hasLdc`, `status`) are all already displayed in the referral body. The entire sections can be removed.

> **Why remove both in the same branch?**  
> The waitlist/caselist DTOs also contain PII (name/crn/dob). Since we're deleting those DTOs entirely, we avoid touching them twice. This branch is "data removal only" — it does not touch the attendance section of the template.

---

## Files to DELETE

These two files should be deleted entirely:

```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestGroupWaitlistItemView.kt
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferralCaseListItemView.kt
```

---

## Files to MODIFY

### 1. `SubjectAccessRequestReferral.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestReferral(
  val id: UUID?,
  val crn: String,
  val dateOfBirth: LocalDate?,
  val personName: String,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val createdAt: LocalDateTime,
  val interventionName: String?,
  val interventionType: String,
  val setting: String,
  val sourcedFrom: String?,
  val deliveryLocationPreference: SubjectAccessRequestDeliveryLocationPreference?,
  val programmeGroupMemberships: MutableSet<SubjectAccessRequestProgrammeGroupMembership>,
  val statusHistories: MutableList<SubjectAccessRequestReferralStatusHistory>,
  val messageHistories: MutableList<SubjectAccessRequestMessageHistory>,
  val referralLdcHistories: MutableSet<SubjectAccessRequestReferralLdcHistory>,
  val referralCohortHistories: MutableSet<SubjectAccessRequestReferralCohortHistory>,
  val referralMotivationBackgroundAndNonAssociation: SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation?,
  val referralReportingLocation: SubjectAccessRequestReferralReportingLocation?,
  val attendees: MutableList<SubjectAccessRequestAttendee>,
  val availability: SubjectAccessRequestAvailability?,
)

fun ReferralEntity.toApi(
  messageHistoryEntities: List<MessageHistoryEntity>,
  attendeeEntities: List<AttendeeEntity>,
  availabilityEntity: AvailabilityEntity?,
) = SubjectAccessRequestReferral(
  id = id,
  crn = crn,
  dateOfBirth = dateOfBirth,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  createdAt = createdAt,
  interventionName = interventionName,
  interventionType = interventionType.name,
  setting = setting.name,
  sourcedFrom = sourcedFrom?.name,
  deliveryLocationPreference = deliveryLocationPreferences?.toApi(),
  programmeGroupMemberships = programmeGroupMemberships.map { it.toApi() }.toMutableSet(),
  statusHistories = statusHistories.map { it.toApi() }.toMutableList(),
  messageHistories = messageHistoryEntities.map { it.toApi() }.toMutableList(),
  referralLdcHistories = referralLdcHistories.map { it.toApi() }.toMutableSet(),
  referralCohortHistories = referralCohortHistories.map { it.toApi() }.toMutableSet(),
  referralMotivationBackgroundAndNonAssociation = referralMotivationBackgroundAndNonAssociations?.toApi(),
  referralReportingLocation = referralReportingLocation?.toApi(),
  attendees = attendeeEntities.map { it.toApi() }.toMutableList(),
  availability = availabilityEntity?.toApi(),
)
```

**New content — remove `crn`, `dateOfBirth`, `personName` from the data class and from the `toApi()` function:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestReferral(
  val id: UUID?,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val createdAt: LocalDateTime,
  val interventionName: String?,
  val interventionType: String,
  val setting: String,
  val sourcedFrom: String?,
  val deliveryLocationPreference: SubjectAccessRequestDeliveryLocationPreference?,
  val programmeGroupMemberships: MutableSet<SubjectAccessRequestProgrammeGroupMembership>,
  val statusHistories: MutableList<SubjectAccessRequestReferralStatusHistory>,
  val messageHistories: MutableList<SubjectAccessRequestMessageHistory>,
  val referralLdcHistories: MutableSet<SubjectAccessRequestReferralLdcHistory>,
  val referralCohortHistories: MutableSet<SubjectAccessRequestReferralCohortHistory>,
  val referralMotivationBackgroundAndNonAssociation: SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation?,
  val referralReportingLocation: SubjectAccessRequestReferralReportingLocation?,
  val attendees: MutableList<SubjectAccessRequestAttendee>,
  val availability: SubjectAccessRequestAvailability?,
)

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
  deliveryLocationPreference = deliveryLocationPreferences?.toApi(),
  programmeGroupMemberships = programmeGroupMemberships.map { it.toApi() }.toMutableSet(),
  statusHistories = statusHistories.map { it.toApi() }.toMutableList(),
  messageHistories = messageHistoryEntities.map { it.toApi() }.toMutableList(),
  referralLdcHistories = referralLdcHistories.map { it.toApi() }.toMutableSet(),
  referralCohortHistories = referralCohortHistories.map { it.toApi() }.toMutableSet(),
  referralMotivationBackgroundAndNonAssociation = referralMotivationBackgroundAndNonAssociations?.toApi(),
  referralReportingLocation = referralReportingLocation?.toApi(),
  attendees = attendeeEntities.map { it.toApi() }.toMutableList(),
  availability = availabilityEntity?.toApi(),
)
```

---

### 2. `SubjectAccessRequestContent.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestContent.kt
```

**Current content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

data class SubjectAccessRequestContent(
  val referrals: List<SubjectAccessRequestReferral>,
  val groupWaitlistItemViews: List<SubjectAccessRequestGroupWaitlistItemView>,
  val referralCaseListItemViews: List<SubjectAccessRequestReferralCaseListItemView>,
)
```

**New content:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

data class SubjectAccessRequestContent(
  val referrals: List<SubjectAccessRequestReferral>,
)
```

---

### 3. `SubjectAccessRequestService.kt`

**Path:**
```
src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SubjectAccessRequestService.kt
```

**Current content (full file):**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestGroupWaitlistItemView
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferralCaseListItemView
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
@Transactional
class SubjectAccessRequestService(
  private val referralRepository: ReferralRepository,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val attendeeRepository: AttendeeRepository,
  private val availabilityRepository: AvailabilityRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
) : HmppsProbationSubjectAccessRequestService {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent {
    log.info("Retrieving subject access request content for crn: $crn")
    val referrals = getSubjectAccessRequestReferrals(crn, fromDate, toDate)
    log.info("Retrieved ${referrals.size} referrals")
    log.info("referrals: $referrals")
    val groupWaitlistItemViews = getSubjectAccessRequestGroupWaitlistItemViews(crn)
    val referralCaseListItemViews = getSubjectAccessRequestReferralCaseListItemViews(crn)
    val content = SubjectAccessRequestContent(
      referrals,
      groupWaitlistItemViews,
      referralCaseListItemViews,
    )

    return HmppsSubjectAccessRequestContent(content)
  }

  private fun getSubjectAccessRequestReferralCaseListItemViews(crn: String): List<SubjectAccessRequestReferralCaseListItemView> {
    val referralCaseListItemViews = referralCaseListItemRepository.findByCrn(crn)

    return referralCaseListItemViews.map { it.toApi() }.toList()
  }

  private fun getSubjectAccessRequestGroupWaitlistItemViews(crn: String): List<SubjectAccessRequestGroupWaitlistItemView> {
    val groupWaitlistItemViews = groupWaitlistItemViewRepository.findByCrn(crn)

    return groupWaitlistItemViews.map { it.toApi() }.toList()
  }

  private fun getSubjectAccessRequestReferrals(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<SubjectAccessRequestReferral> {
    val referrals = referralRepository.findByCrn(crn)

    return referrals.filter { referral ->
      val afterFromDate = fromDate?.let { referral.createdAt.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { referral.createdAt.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }.map { referralEntity ->
      val messageHistoryEntities = messageHistoryRepository.findByReferral(referralEntity)
      val attendeeEntities = attendeeRepository.findByReferral(referralEntity)
      val availabilityEntity = referralEntity.id?.let { availabilityRepository.findByReferralId(it) }
      referralEntity.toApi(messageHistoryEntities, attendeeEntities, availabilityEntity)
    }.toList()
  }
}
```

**New content — remove `GroupWaitlistItemViewRepository`, `ReferralCaseListItemRepository` and all associated code:**
```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
@Transactional
class SubjectAccessRequestService(
  private val referralRepository: ReferralRepository,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val attendeeRepository: AttendeeRepository,
  private val availabilityRepository: AvailabilityRepository,
) : HmppsProbationSubjectAccessRequestService {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent {
    log.info("Retrieving subject access request content for crn: $crn")
    val referrals = getSubjectAccessRequestReferrals(crn, fromDate, toDate)
    log.info("Retrieved ${referrals.size} referrals")
    log.info("referrals: $referrals")
    val content = SubjectAccessRequestContent(referrals)

    return HmppsSubjectAccessRequestContent(content)
  }

  private fun getSubjectAccessRequestReferrals(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<SubjectAccessRequestReferral> {
    val referrals = referralRepository.findByCrn(crn)

    return referrals.filter { referral ->
      val afterFromDate = fromDate?.let { referral.createdAt.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { referral.createdAt.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }.map { referralEntity ->
      val messageHistoryEntities = messageHistoryRepository.findByReferral(referralEntity)
      val attendeeEntities = attendeeRepository.findByReferral(referralEntity)
      val availabilityEntity = referralEntity.id?.let { availabilityRepository.findByReferralId(it) }
      referralEntity.toApi(messageHistoryEntities, attendeeEntities, availabilityEntity)
    }.toList()
  }
}
```

---

### 4. `sar_template.mustache`

**Path:**
```
src/main/resources/sar_template.mustache
```

**Changes — two separate removals:**

**Removal A:** Lines 7–9 — remove the 3 PII rows from the Referral Details table.

Remove these 3 lines:
```html
    <tr><td>Person Name</td><td>{{ optionalValue personName }}</td></tr>
    <tr><td>CRN</td><td>{{ optionalValue crn }}</td></tr>
    <tr><td>Date of Birth</td><td>{{ formatDate dateOfBirth }}</td></tr>
```

The table should go from:
```html
  <table class="summary-list">
    <tr><td>Person Name</td><td>{{ optionalValue personName }}</td></tr>
    <tr><td>CRN</td><td>{{ optionalValue crn }}</td></tr>
    <tr><td>Date of Birth</td><td>{{ formatDate dateOfBirth }}</td></tr>
    <tr><td>Sex</td><td>{{ optionalValue sex }}</td></tr>
    <tr><td>Sentence End Date</td><td>{{ formatDate sentenceEndDate }}</td></tr>
```

To:
```html
  <table class="summary-list">
    <tr><td>Sex</td><td>{{ optionalValue sex }}</td></tr>
    <tr><td>Sentence End Date</td><td>{{ formatDate sentenceEndDate }}</td></tr>
```

**Removal B:** Lines 266–322 — delete the entire Group Waitlist Items section and Referral Case List Items section.

Remove from:
```html
<h2>Group Waitlist Items</h2>
```
...all the way to the end of the file (the closing `{{/referralCaseListItemViews.0}}` and trailing newline). The file currently ends at line 323.

After the `{{/referrals}}` block (line 264–265) and the `<hr/>`, the file should end at:
```html
</section>
<hr/>
{{/referrals}}
```

The final file should end immediately after `{{/referrals}}` with no trailing content.

---

### 5. `SubjectAccessRequestServiceTest.kt` (unit test) — REQUIRED

**Path:**
```
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SubjectAccessRequestServiceTest.kt
```

This unit test directly wires `SubjectAccessRequestService` with 6 mocked repositories (including the two we removed) and asserts on the removed DTOs/fields. **It will not compile without these edits.**

**Changes required:**

1. **Remove these imports** (lines 11, 13, 26, 28):
   ```kotlin
   import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.GroupWaitlistItemViewEntityFactory
   import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCaseListItemViewEntityFactory
   import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
   import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
   ```

2. **Remove these `mockk` fields** (lines 40–41):
   ```kotlin
   private val groupWaitlistItemViewRepository = mockk<GroupWaitlistItemViewRepository>()
   private val referralCaseListItemRepository = mockk<ReferralCaseListItemRepository>()
   ```

3. **Update the service constructor call** in `setup()` (lines 49–56). Currently:
   ```kotlin
   service = SubjectAccessRequestService(
     referralRepository,
     messageHistoryRepository,
     attendeeRepository,
     availabilityRepository,
     groupWaitlistItemViewRepository,
     referralCaseListItemRepository,
   )
   ```
   Change to:
   ```kotlin
   service = SubjectAccessRequestService(
     referralRepository,
     messageHistoryRepository,
     attendeeRepository,
     availabilityRepository,
   )
   ```

4. **In the `should get probation content for CRN` test**, remove:
   - Lines 113–114 (factory calls for `groupWaitlistItemViewEntity`, `referralCaseListItemViewEntity`)
   - Lines 120–121 (`every { groupWaitlistItemViewRepository... }` and `every { referralCaseListItemRepository... }` stubs)
   - Lines 135–137 — assertions on `crn`, `dateOfBirth`, `personName`:
     ```kotlin
     assertThat(resultContent.referrals[0].crn).isEqualTo(referralEntity1.crn)
     assertThat(resultContent.referrals[0].dateOfBirth).isEqualTo(referralEntity1.dateOfBirth)
     assertThat(resultContent.referrals[0].personName).isEqualTo(referralEntity1.personName)
     ```
   - Lines 262–281 — the whole block asserting `resultContent.groupWaitlistItemViews[...]` and `resultContent.referralCaseListItemViews[...]`
   - Lines 286–287 — the `verify` calls for the two repositories

5. **In the `should get probation content for CRN that doesn't exist` test**, remove:
   - Lines 298–299 (`every { groupWaitlistItemViewRepository... }` stubs)
   - Lines 309–310 (`assertThat(resultContent.groupWaitlistItemViews)...`  and `.referralCaseListItemViews`)
   - Lines 313–314 (`verify` calls for the two repositories)

### 6. `SarContractIntegrationTest.kt` and `HmppsSubjectAccessRequestControllerIntegrationTest.kt`

**Paths:**
```
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/SarContractIntegrationTest.kt
src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/sar/HmppsSubjectAccessRequestControllerIntegrationTest.kt
```

**`SarContractIntegrationTest.kt`** — no code changes needed. Fixtures (`sar-api-response.json`, `sar-expected-render-result.html`) are regenerated by the runbook script (`scripts/local-scripts/regenerate-sar-snapshots.sh`).

**`HmppsSubjectAccessRequestControllerIntegrationTest.kt`** — **DOES** need editing. Originally missed in the plan (see PR #860 report and Tracker correction #6). The `should return 200 on GET subject access request data` test asserts on the raw JSON payload map returned by the SAR endpoint:

```kotlin
val content = response.content as LinkedHashMap<*, *>
assertThat(content.containsKey("referrals")).isTrue()
assertThat(content.get("referrals")).isNotNull()
assertThat(content.containsKey("groupWaitlistItemViews")).isTrue()   // ← remove
assertThat(content.get("groupWaitlistItemViews")).isNotNull()        // ← remove
assertThat(content.containsKey("referralCaseListItemViews")).isTrue() // ← remove
assertThat(content.get("referralCaseListItemViews")).isNotNull()     // ← remove
```

After Branch 1 drops those two fields from `SubjectAccessRequestContent`, the four `containsKey`/`get` assertion lines for `groupWaitlistItemViews` and `referralCaseListItemViews` must be deleted. Keep the two `referrals`-key assertions untouched. The test still exercises the full authorisation + wiring path, so no coverage is lost.

### 7. Factories left intact (verify at execution time!)

The original plan said:
> Do NOT delete `GroupWaitlistItemViewEntityFactory` or `ReferralCaseListItemViewEntityFactory` — they are used by other tests (`GroupWaitlistViewRepositoryIntegrationTest`, `CaseListControllerIntegrationTest`) and by production services (`ProgrammeGroupService`, `ReferralCaseListItemService`) via the entity classes themselves.

**This claim was outdated on `main` at planning time.** Post–PR #860 grep showed zero external call-sites for either factory — see Tracker correction #7 and the follow-up branch `APG-2580/cleanup-orphaned-sar-factories`, which deletes them once PR #860 has merged. If you’re implementing Branch 1 from this doc verbatim (which you should) still do NOT delete the factories here — the sweep-up PR owns that change.

---

## Test Fixture Regeneration

After making the code changes above, regenerate the test fixtures with:

```bash
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "*.SarContractIntegrationTest"
```

This generates two files:
- `build/.../sar-api-response.json.log`  
- `build/.../sar-generated-report.html.log`

The exact output path is determined by the `sarIntegrationTestHelper.saveContentToFile()` library method — check the console output for the file path.

Then copy the generated files over the test resources:
```bash
cp <path-to>/sar-api-response.json.log src/test/resources/sar/sar-api-response.json
cp <path-to>/sar-generated-report.html.log src/test/resources/sar/sar-expected-render-result.html
```

Finally, run without the flag to confirm tests pass:
```bash
./gradlew test --tests "*.SarContractIntegrationTest"
```

---

## Verification Checklist

- [ ] `SubjectAccessRequestGroupWaitlistItemView.kt` deleted
- [ ] `SubjectAccessRequestReferralCaseListItemView.kt` deleted
- [ ] `SubjectAccessRequestReferral.kt` — `crn`, `personName`, `dateOfBirth` removed from data class and `toApi()`
- [ ] `SubjectAccessRequestContent.kt` — only `referrals` field remains
- [ ] `SubjectAccessRequestService.kt` — `GroupWaitlistItemViewRepository` and `ReferralCaseListItemRepository` removed from constructor and all methods
- [ ] `SubjectAccessRequestServiceTest.kt` — updated: removed factories/repos/mocks/assertions for waitlist + caselist; removed PII field assertions
- [ ] `sar_template.mustache` — PII rows removed from referral table; entire waitlist + caselist sections removed
- [ ] `./gradlew build` compiles cleanly
- [ ] SAR contract integration test compiles without errors
- [ ] `./gradlew test --tests "*.SubjectAccessRequestServiceTest"` passes
- [ ] `./gradlew test --tests "*.SarContractIntegrationTest"` passes
- [ ] Test fixtures updated to reflect new output

