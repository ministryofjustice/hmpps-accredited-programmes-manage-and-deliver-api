# APG-2580 SAR Community Update - Branch Plan Index

**Ticket:** APG-2580 - TECH: Update SAR endpoint and report for Community following UAT  
**Total estimated effort:** ~3.5 days  
**Last updated:** 2026-08-13

---

## Ticket Requirements

The ticket asks for four things:

1. **Remove PII** - Name, CRN, Date of Birth
2. **Replace codes/IDs with descriptive values** - Programme name (already OK), attendance outcomes, and status/enum fields generally
3. **Review the Waitlist / Referral Case List** - confirm duplication and if so remove
4. **Attendance and Session Recording** - remove "Created By" from the attendance record, keep "Recorded By", and group attendance + outcome + note as a single logical record

---

## Requirement to Branch Traceability

| Ticket requirement | Addressed by branch |
|--------------------|--------------------|
| Remove Name / CRN / DOB | Branch 1 |
| Remove Waitlist + Case List sections (duplicates confirmed) | Branch 1 |
| Attendance outcome codes (`ATTC` -> `Attended`) | Branch 2 |
| Programme name shows "Building Choices" | Already implemented - no change needed |
| All other system enum codes (interventionType, setting, sourcedFrom, pathway, sessionType, slotName) | Branch 3 |
| Status history text (e.g. "Scheduled") | Already stored as human-readable in DB - no change needed |
| Remove "Created By" from attendance; group attendance + outcome + note | Branch 4 |
| Session note "Recorded By" (use full name, not username lookup) | Branch 4 |

Every element of the ticket is covered.

---

## Overview

```
main
 |-- APG-2580/remove-pii-and-duplicate-sections          [Branch 1]
     |-- APG-2580/attendance-codes-to-descriptions       [Branch 2]
         |-- APG-2580/enum-display-names                 [Branch 3]
             |-- APG-2580/attendance-session-note-restructure  [Branch 4]
```

Each branch is small, focused, independently testable, and depends on the previous.

---

## Branch Summary

| # | Branch | Requirement | Effort | Document |
|---|--------|-------------|--------|----------|
| 1 | `APG-2580/remove-pii-and-duplicate-sections` | Remove Name/CRN/DOB; remove Group Waitlist + Referral Case List sections | 1 day | [APG-2580-branch-1-remove-pii-and-duplicate-sections.md](./APG-2580-branch-1-remove-pii-and-duplicate-sections.md) |
| 2 | `APG-2580/attendance-codes-to-descriptions` | Replace `ATTC` attendance codes with descriptions like `Attended - Complied` | 0.5 days | [APG-2580-branch-2-attendance-codes-to-descriptions.md](./APG-2580-branch-2-attendance-codes-to-descriptions.md) |
| 3 | `APG-2580/enum-display-names` | Add display names to InterventionType, SettingType, Pathway, SourcedFrom; use `.value` for SessionType; use `.displayName` for SlotName | 0.5 days | [APG-2580-branch-3-enum-display-names.md](./APG-2580-branch-3-enum-display-names.md) |
| 4 | `APG-2580/attendance-session-note-restructure` | Remove `Created By`/`Created At` from attendance; group attendance + outcome + notes as single record; fix note `Recorded By` field | 1 day | [APG-2580-branch-4-attendance-session-note-restructure.md](./APG-2580-branch-4-attendance-session-note-restructure.md) |
| - | Buffer for fixture regen + UAT review | | 0.5 days | - |

---

## Files Touched Across All Branches

### Production code

| File | Branch(es) | Change |
|------|-----------|--------|
| `api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt` | 1, 3 | B1: remove `crn`, `personName`, `dateOfBirth`. B3: use `.displayName` for enums |
| `api/model/subjectAccessRequest/SubjectAccessRequestContent.kt` | 1 | Remove `groupWaitlistItemViews`, `referralCaseListItemViews` |
| `api/model/subjectAccessRequest/SubjectAccessRequestGroupWaitlistItemView.kt` | 1 | **DELETE FILE** |
| `api/model/subjectAccessRequest/SubjectAccessRequestReferralCaseListItemView.kt` | 1 | **DELETE FILE** |
| `service/SubjectAccessRequestService.kt` | 1 | Remove waitlist/caselist repository calls |
| `resources/sar_template.mustache` | 1, 2, 4 | B1: remove PII rows + waitlist section. B2: `outcomeType.outcomeTypeCode` -> `outcomeType.outcomeDescription`. B4: restructure attendance section |
| `api/model/subjectAccessRequest/SubjectAccessRequestSessionAttendanceNDeliusOutcome.kt` | 2 | Rename `outcomeTypeCode` -> `outcomeDescription`, map from `description` |
| `entity/type/InterventionType.kt` | 3 | Add `displayName` constructor property |
| `entity/type/SettingType.kt` | 3 | Add `displayName` |
| `entity/type/Pathway.kt` | 3 | Add `displayName` |
| `entity/ReferralEntity.kt` | 3 | Add `displayName` to `ReferralEntitySourcedFrom` enum |
| `api/model/subjectAccessRequest/SubjectAccessRequestSession.kt` | 3 | Use `.displayName` / `.value` in mapper |
| `api/model/subjectAccessRequest/SubjectAccessRequestAvailabilitySlot.kt` | 3 | Use full day name + capitalised slot display name |
| `api/model/subjectAccessRequest/SubjectAccessRequestSessionAttendance.kt` | 4 | Remove `createdBy`, `createdAt` |
| `api/model/subjectAccessRequest/SubjectAccessRequestSessionNoteHistory.kt` | 4 | Replace `createdBy` (username) with `recordedBy` (mapped from `createdByFullName`) |

### Test fixtures (regenerated in each branch)

| File | Regenerated in |
|------|---------------|
| `src/test/resources/sar/sar-api-response.json` | Branches 1, 2, 3, 4 |
| `src/test/resources/sar/sar-expected-render-result.html` | Branches 1, 2, 3, 4 |

### Test code

| File | Branch | Change |
|------|--------|--------|
| `sar/SarContractIntegrationTest.kt` | 1 | Remove `GroupWaitlistItemViewRepository` + `ReferralCaseListItemRepository` autowired fields and imports if present |

---

## Files NOT Touched (Verified Clean)

These SAR model files are unchanged across all branches:

- `SubjectAccessRequestAccreditedProgrammeTemplate.kt` - already correct (`name` = "Building Choices")
- `SubjectAccessRequestDeliveryLocationPreference.kt`
- `SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation.kt`
- `SubjectAccessRequestReferralStatusHistory.kt` - already renders "Scheduled" etc.
- `SubjectAccessRequestReferralStatusDescription.kt`
- `SubjectAccessRequestProgrammeGroupMembership.kt`
- `SubjectAccessRequestProgrammeGroup.kt`
- `SubjectAccessRequestFacilitator.kt`
- `SubjectAccessRequestAttendee.kt`
- `SubjectAccessRequestAvailability.kt`
- `SubjectAccessRequestMessageHistory.kt`
- `SubjectAccessRequestReferralLdcHistory.kt`
- `SubjectAccessRequestReferralCohortHistory.kt` - cohort already stored as "General offence"
- `SubjectAccessRequestReferralReportingLocation.kt`

---

## How to Regenerate Test Fixtures (applies to all branches)

After each set of code changes, run:

```bash
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "*.SarContractIntegrationTest"
```

Check the console output for the `.log` file paths. Copy them over the fixture files:

```bash
cp <log-output-path>/sar-api-response.json.log src/test/resources/sar/sar-api-response.json
cp <log-output-path>/sar-generated-report.html.log src/test/resources/sar/sar-expected-render-result.html
```

Then run without the flag to confirm tests pass:

```bash
./gradlew test --tests "*.SarContractIntegrationTest"
```

---

## Confidence & Verification

All facts in these documents have been verified against the codebase directly (August 2026), including:

- Waitlist/CaseList SQL views inspected - confirmed all remaining fields (after PII removal) are already present in the referral section
- `SessionAttendanceNDeliusOutcomeEntity.description` field confirmed to exist in DB and be populated by factory (`"Attended - Complied"`)
- `SessionNotesHistoryEntity.createdByFullName` field confirmed to exist
- `SessionAttendanceEntity` has BOTH `createdBy` (Spring `@CreatedBy` username) AND `recordedByFacilitator` (linked FacilitatorEntity) - safe to remove `createdBy` from SAR
- `SessionType` enum already has `.value` display name; `SlotName` already has `.displayName`
- `InterventionType`, `SettingType`, `Pathway`, `ReferralEntitySourcedFrom` do NOT have display names - Branch 3 adds them
- SAR contract integration test uses `SAR_GENERATE_ACTUAL=true` env flag to write `.log` files, which the developer copies over test fixtures manually
- Integration test uses `@SpringBootTest(webEnvironment = RANDOM_PORT)` with Testcontainers PostgreSQL + LocalStack - first run boots Docker containers (~2-3 min)

---

## Open Points to Confirm with Ticket Author / Product

1. **Branch 3 display names for `InterventionType`** - best-guess expansions used:
   - `SI` -> "Structured Intervention"
   - `ACP` -> "Accredited Programme"
   - `CRS` -> "Commissioned Rehabilitative Service"
   - `TOOLKITS` -> "Toolkits"

   These should be verified with a service-side stakeholder.

2. **Branch 4 - session note `recordedBy` is null in the test fixture** because the integration test factory does not populate `createdByFullName`. In production this will be set. Confirm the team is happy for the test fixture to show `null` here.
