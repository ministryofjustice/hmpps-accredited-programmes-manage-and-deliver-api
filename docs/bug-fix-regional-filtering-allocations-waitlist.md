# Bug Fix Plan: Regional Filtering — Group Allocations & Waitlist

**Ticket:** APG (bug) — Group Allocations and Waitlist displaying referrals from outside the logged-in user's region
**Environment:** Pre-prod
**Status:** Root cause confirmed in API — fix plan documented here
**Date:** 2026-05-20

---

## Summary

The `GET /bff/group/{groupId}/WAITLIST` endpoint returns referrals from **all regions**, not just the logged-in user's region. The UI is not at fault — it correctly delegates regional access control to the API.

There are **two confirmed bugs** and **one secondary issue** with the filter dropdown data.

---

## Root Cause 1 (PRIMARY) — WAITLIST query has no region filter

**File:** `src/main/kotlin/.../repository/specification/GetGroupWaitlistItemSpecification.kt`
**Lines:** 27–51 (the `WAITLIST` branch of the `when` block)

The WAITLIST tab queries the `group_waitlist_item_view` materialized view for:
```
status == "Awaiting allocation" AND activeProgrammeGroupId IS NULL
```

There is **no predicate on `regionName`**. The view contains every "Awaiting allocation" referral from every region in the country. An East Midlands user will therefore see Greater Manchester, London, Yorkshire referrals in their waitlist.

The `regionName` column exists on the view and has an index (`idx_group_wait_list_region_name`) — it was clearly intended for this purpose but was never wired up.

---

## Root Cause 2 (SECONDARY) — Filter dropdown lists PDUs from all regions

**File:** `src/main/kotlin/.../repository/ReferralReportingLocationRepository.kt`
**Line:** 13

```kotlin
@Query("SELECT DISTINCT r.pduName, r.reportingTeam FROM ReferralReportingLocationEntity r")
fun getPdusAndReportingTeams(): List<PduReportingLocation>
```

This query has no region constraint — it returns every PDU and reporting team in the database. The filter dropdown on the Allocations/Waitlist page therefore shows Greater Manchester PDUs to an East Midlands user.

---

## How the existing pattern works (for context)

The neighbouring endpoint `GET /bff/groups/{selectedTab}` already does this correctly. Here is the pattern you need to replicate:

**Controller** resolves the username from the request:
```kotlin
// ProgrammeGroupController.kt — getProgrammeGroupsByRegion (line 212)
val username = authenticationUtils.getUsername()
val groups = programmeGroupService.getProgrammeGroupsForRegion(
  ...,
  username,
)
```

**Service** resolves the user's region description from the username:
```kotlin
// ProgrammeGroupService.kt — createGroup (line 91) — same pattern
val (userRegion) = userService.getUserRegions(username)
// userRegion.description is a String e.g. "East Midlands"
```

You need to apply this same pattern to `getGroupAllocations` and `getGroupWaitlistDataByCriteria`.

---

## Fix Plan — Step by Step

### Step 1 — Controller: resolve the username

**File:** `src/main/kotlin/.../api/controller/ProgrammeGroupController.kt`
**Function:** `getGroupAllocations` (line 125)

Add `val username = authenticationUtils.getUsername()` and pass it to the service (exactly as `getProgrammeGroupsByRegion` does at line 212):

```kotlin
// BEFORE
fun getGroupAllocations(
  pageable: Pageable,
  groupId: UUID,
  selectedTab: GroupPageTab,
  sex: String?,
  cohort: String?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
): ResponseEntity<ProgrammeGroupAllocations> {
  val groupCohort = if (cohort.isNullOrEmpty()) null else ProgrammeGroupCohort.fromString(cohort)

  val programmeDetails = programmeGroupService.getGroupWaitlistDataByCriteria(
    pageable,
    selectedTab,
    groupId,
    sex,
    groupCohort,
    nameOrCRN,
    pdu,
    reportingTeams,
  )

  return ResponseEntity.ok(programmeDetails)
}
```

```kotlin
// AFTER
fun getGroupAllocations(
  pageable: Pageable,
  groupId: UUID,
  selectedTab: GroupPageTab,
  sex: String?,
  cohort: String?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
): ResponseEntity<ProgrammeGroupAllocations> {
  val username = authenticationUtils.getUsername()  // <-- ADD THIS
  val groupCohort = if (cohort.isNullOrEmpty()) null else ProgrammeGroupCohort.fromString(cohort)

  val programmeDetails = programmeGroupService.getGroupWaitlistDataByCriteria(
    pageable,
    selectedTab,
    groupId,
    sex,
    groupCohort,
    nameOrCRN,
    pdu,
    reportingTeams,
    username,  // <-- ADD THIS
  )

  return ResponseEntity.ok(programmeDetails)
}
```

---

### Step 2 — Service: resolve the region and pass it to the spec and the filter

**File:** `src/main/kotlin/.../service/ProgrammeGroupService.kt`
**Function:** `getGroupWaitlistDataByCriteria` (line 339)

```kotlin
// BEFORE
fun getGroupWaitlistDataByCriteria(
  pageable: Pageable,
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: ProgrammeGroupCohort?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
): ProgrammeGroupAllocations {
  val group = programmeGroupRepository.findByIdOrNull(groupId)
    ?: throw NotFoundException("Programme group with id $groupId not found")

  val otherTab = if (selectedTab === GroupPageTab.WAITLIST) GroupPageTab.ALLOCATED else GroupPageTab.WAITLIST

  val activeSpecification =
    getGroupWaitlistItemSpecification(selectedTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

  val nonActiveSpecification =
    getGroupWaitlistItemSpecification(otherTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

  val groupListDataToReturn: Page<GroupItem> =
    groupWaitlistItemViewRepository.findAll(activeSpecification, pageable).map { it.toApi() }

  val otherTabCount: Int = groupWaitlistItemViewRepository.count(nonActiveSpecification).toInt()

  return ProgrammeGroupAllocations(
    group = Group(
      id = group.id,
      code = group.code,
      regionName = group.regionName,
    ),
    filters = getGroupAllocationsFilters(),
    pagedGroupData = groupListDataToReturn,
    otherTabTotal = otherTabCount,
  )
}
```

```kotlin
// AFTER
fun getGroupWaitlistDataByCriteria(
  pageable: Pageable,
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: ProgrammeGroupCohort?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
  username: String,  // <-- ADD THIS PARAMETER
): ProgrammeGroupAllocations {
  val group = programmeGroupRepository.findByIdOrNull(groupId)
    ?: throw NotFoundException("Programme group with id $groupId not found")

  val (userRegion) = userService.getUserRegions(username)  // <-- RESOLVE REGION
  val userRegionName = userRegion.description               // e.g. "East Midlands"

  val otherTab = if (selectedTab === GroupPageTab.WAITLIST) GroupPageTab.ALLOCATED else GroupPageTab.WAITLIST

  val activeSpecification =
    getGroupWaitlistItemSpecification(selectedTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams, userRegionName)  // <-- PASS REGION

  val nonActiveSpecification =
    getGroupWaitlistItemSpecification(otherTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams, userRegionName)     // <-- PASS REGION

  val groupListDataToReturn: Page<GroupItem> =
    groupWaitlistItemViewRepository.findAll(activeSpecification, pageable).map { it.toApi() }

  val otherTabCount: Int = groupWaitlistItemViewRepository.count(nonActiveSpecification).toInt()

  return ProgrammeGroupAllocations(
    group = Group(
      id = group.id,
      code = group.code,
      regionName = group.regionName,
    ),
    filters = getGroupAllocationsFilters(userRegionName),  // <-- PASS REGION
    pagedGroupData = groupListDataToReturn,
    otherTabTotal = otherTabCount,
  )
}
```

Also update `getGroupAllocationsFilters` (line 404) to accept and use the region:

```kotlin
// BEFORE
fun getGroupAllocationsFilters(): ProgrammeGroupAllocations.ProgrammeGroupAllocationsFilters {
  val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
  ...
}

// AFTER
fun getGroupAllocationsFilters(userRegionName: String): ProgrammeGroupAllocations.ProgrammeGroupAllocationsFilters {
  val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams(userRegionName)  // <-- PASS REGION
  ...
}
```

---

### Step 3 — Specification: add the regionName predicate for WAITLIST

**File:** `src/main/kotlin/.../repository/specification/GetGroupWaitlistItemSpecification.kt`

```kotlin
// BEFORE
fun getGroupWaitlistItemSpecification(
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: ProgrammeGroupCohort?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
): Specification<GroupWaitlistItemViewEntity> = Specification { root, _, criteriaBuilder ->
  val predicates = mutableListOf<Predicate>()

  when (selectedTab) {
    GroupPageTab.ALLOCATED -> predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))
    GroupPageTab.WAITLIST -> {
      predicates.add(criteriaBuilder.equal(root.get<String>("status"), "Awaiting allocation"))
      predicates.add(criteriaBuilder.isNull(root.get<UUID>("activeProgrammeGroupId")))
      // ... other filters
    }
  }
  ...
}
```

```kotlin
// AFTER
fun getGroupWaitlistItemSpecification(
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: ProgrammeGroupCohort?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
  userRegionName: String,  // <-- ADD THIS PARAMETER
): Specification<GroupWaitlistItemViewEntity> = Specification { root, _, criteriaBuilder ->
  val predicates = mutableListOf<Predicate>()

  when (selectedTab) {
    GroupPageTab.ALLOCATED -> predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))
    GroupPageTab.WAITLIST -> {
      predicates.add(criteriaBuilder.equal(root.get<String>("regionName"), userRegionName))  // <-- ADD THIS (the fix!)
      predicates.add(criteriaBuilder.equal(root.get<String>("status"), "Awaiting allocation"))
      predicates.add(criteriaBuilder.isNull(root.get<UUID>("activeProgrammeGroupId")))
      // ... rest of filters remain unchanged
    }
  }
  ...
}
```

---

### Step 4 — Repository: add region-scoped PDU query

**File:** `src/main/kotlin/.../repository/ReferralReportingLocationRepository.kt`

```kotlin
// BEFORE
@Query("SELECT DISTINCT r.pduName, r.reportingTeam FROM ReferralReportingLocationEntity r")
fun getPdusAndReportingTeams(): List<PduReportingLocation>

// AFTER
@Query("SELECT DISTINCT r.pduName, r.reportingTeam FROM ReferralReportingLocationEntity r WHERE r.regionName = :regionName")
fun getPdusAndReportingTeams(regionName: String): List<PduReportingLocation>
```

> Note: Check that `ReferralReportingLocationEntity` has a `regionName` field. If it doesn't, you may need to join to the referral table to get the region. Check the entity first before writing this query.

---

## Files to change (summary)

| File | Change |
|------|--------|
| `api/controller/ProgrammeGroupController.kt` | Add `authenticationUtils.getUsername()` call, pass `username` to service |
| `service/ProgrammeGroupService.kt` | Accept `username`, resolve `userRegion`, pass `userRegionName` to spec + filter |
| `repository/specification/GetGroupWaitlistItemSpecification.kt` | Accept `userRegionName`, add `regionName` predicate to WAITLIST branch |
| `repository/ReferralReportingLocationRepository.kt` | Add region-scoped overload of `getPdusAndReportingTeams` |

---

## Tests to add / update

### Integration test — WAITLIST tab only shows user's region

**File:** `src/test/kotlin/.../api/controller/ProgrammeGroupControllerIntegrationTest.kt`

Add a test case:
```
Given referrals exist in East Midlands AND Greater Manchester
And the user is an East Midlands programme team member
When GET /bff/group/{eastMidlandsGroupId}/WAITLIST is called
Then only East Midlands referrals are returned
And Greater Manchester referrals are NOT in the response
```

### Integration test — filter dropdown only shows user's region PDUs

```
Given PDUs exist for East Midlands AND Greater Manchester
And the user is an East Midlands programme team member
When GET /bff/group/{eastMidlandsGroupId}/WAITLIST is called
Then filters.locationFilterValues only contains East Midlands PDUs
```

### Unit test — spec

In the existing spec unit tests, add a case asserting the `regionName` predicate is present when `selectedTab == WAITLIST`.

---

## How to check `ReferralReportingLocationEntity` has regionName

Run:
```bash
find src -name "ReferralReportingLocationEntity.kt" | xargs cat
```

If `regionName` is not a field, the query in Step 4 will need to join via `ReferralEntity`:
```kotlin
@Query("""
  SELECT DISTINCT r.pduName, r.reportingTeam 
  FROM ReferralReportingLocationEntity r 
  JOIN ReferralEntity ref ON ref.id = r.referral.id 
  WHERE r.regionName = :regionName
""")
```
(Adapt based on the actual entity relationship.)

---

## What is NOT causing this bug

- **The UI** — confirmed clear. No UI fix needed.
- **The data importer** — the `region_name` column in the view is populated from `referral_reporting_location.region_name`. If some referrals have `'No information'` as their region (missing import data), those referrals will fail the `regionName = 'East Midlands'` predicate after the fix and correctly won't appear. Pre-prod dirty data may still need a one-off reprocess after the fix is applied but is not the root cause.
- **The ALLOCATED tab** — filtered by `activeProgrammeGroupId = groupId` so already scoped to one specific group. Low risk.

---

## Quick reference — where to find the pattern in the existing codebase

| Concept | Where to look |
|---------|--------------|
| `authenticationUtils.getUsername()` | `ProgrammeGroupController.kt` line 212 |
| `userService.getUserRegions(username)` | `ProgrammeGroupService.kt` lines 91, 379, 701 |
| `userRegion.description` (the string e.g. "East Midlands") | `ProgrammeGroupService.kt` line 92 |
| Another spec that filters by regionName | `GetProgrammeGroupsSpecification.kt` |

