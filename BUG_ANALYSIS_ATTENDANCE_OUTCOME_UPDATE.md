# Bug Analysis: Session Attendance Outcome Cannot Be Changed

## Executive Summary

**Issue**: Users are unable to change the attendance outcome on a session. When they attempt to update the outcome (e.g., from "Attended" to "Did not attend"), the UI displays a success message, but the outcome change is not persisted to the database or synchronized with nDelius.

**Severity**: HIGH - Core functionality broken for attendance recording

**Root Cause**: The `saveSessionAttendance` method in `SessionService` contains faulty logic that filters attendance records to determine which ones have "changed". The filter only checks if session notes have changed, but completely ignores whether the outcome code has changed.

**Timeline**: 
- First outcome can be recorded ✓
- Changing an existing outcome fails ✗

---

## Detailed Analysis

### 1. Codebase Structure

#### Attendance Flow Architecture:
```
SessionController
  ↓ POST /session/{sessionId}/attendance
  ↓ calls sessionService.saveSessionAttendance()
SessionService.saveSessionAttendance()
  ↓ Validates referrals are session attendees
  ↓ Filters "changed attendees" ← BUG IS HERE
  ↓ Creates SessionAttendanceEntity objects
  ↓ Saves to database
  ↓ Updates nDelius appointments
```

#### Key Files Involved:
1. **SessionController.kt** (Line 603-612)
   - Endpoint: `POST /session/{sessionId}/attendance`
   - Calls: `sessionService.saveSessionAttendance(sessionId, sessionAttendance)`

2. **SessionService.kt** (Line 477-580)
   - Method: `saveSessionAttendance()`
   - Contains the buggy filter logic (Lines 502-507)

3. **SessionAttendanceEntity.kt**
   - Database entity storing attendance records
   - Fields:
     - `id: UUID`
     - `session: SessionEntity`
     - `groupMembership: ProgrammeGroupMembershipEntity`
     - `outcomeType: SessionAttendanceNDeliusOutcomeEntity` ← The outcome
     - `notesHistory: List<SessionNotesHistoryEntity>` ← The notes
     - `createdAt: LocalDateTime`

4. **SessionAttendee.kt** (API Model)
   - Fields:
     - `referralId: UUID`
     - `outcomeCode: SessionAttendanceNDeliusCode` ← Outcome being submitted
     - `sessionNotes: String?` ← Notes being submitted

5. **SessionAttendanceNDeliusCode.kt** (Enum)
   - Possible values:
     - `ATTC` - Attended - Complied
     - `AFTC` - Attended - Failed to Comply
     - `UAAB` - Unacceptable Absence

---

### 2. Bug Details

#### Location
- **File**: `src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SessionService.kt`
- **Method**: `saveSessionAttendance()`
- **Lines**: 502-507

#### Buggy Code
```kotlin
// Only record attendees with something new either a first outcome or changed notes. Unchanged resubmissions
// must not reach nDelius, which appends the notes on every update.
val changedAttendees = sessionAttendance.attendees.filter { attendee ->
  val latestAttendance = latestAttendanceByReferralId[attendee.referralId] ?: return@filter true
  val submittedNotes = attendee.sessionNotes?.trim()
  val latestNotes = latestAttendance.notesHistory.maxByOrNull { it.createdAt }?.notes?.trim()
  !submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes
}
```

#### Problem Analysis

The filter logic incorrectly determines which attendees have "changed":

1. **Case 1 - First time recording attendance** (latestAttendance is null)
   - Filter: `return@filter true` → **INCLUDED** ✓
   - Status: **WORKS** - First outcome is recorded

2. **Case 2 - Changing outcome WITHOUT changing notes** (latestAttendance exists, notes unchanged)
   - Submitted: `outcomeCode = UAAB, sessionNotes = null`
   - Latest outcome: `ATTC`
   - Latest notes: `null`
   - Filter: `!submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes`
   - Evaluation: `!null.isNullOrEmpty() && null != null` = `false && false` = **FALSE**
   - Status: **EXCLUDED** ✗ - Outcome change is LOST

3. **Case 3 - Changing outcome AND notes**
   - Submitted: `outcomeCode = UAAB, sessionNotes = "New notes"`
   - Latest outcome: `ATTC`
   - Latest notes: `"Old notes"`
   - Filter: `!submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes`
   - Evaluation: `!"New notes".isNullOrEmpty() && "New notes" != "Old notes"` = `true && true` = **TRUE**
   - Status: **INCLUDED** ✓ - Both outcome and notes are recorded

4. **Case 4 - Changing notes but not outcome**
   - Submitted: `outcomeCode = ATTC, sessionNotes = "New notes"`
   - Latest outcome: `ATTC`
   - Latest notes: `"Old notes"`
   - Filter: `!"New notes".isNullOrEmpty() && "New notes" != "Old notes"` = **TRUE**
   - Status: **INCLUDED** ✓ - Notes change is recorded (outcome unchanged is OK)

#### Consequence
When an outcome is changed without changing notes:
1. The attendee is excluded from `changedAttendees` list
2. No new `SessionAttendanceEntity` is created
3. The change is never saved to database
4. The change is never sent to nDelius
5. UI shows success message, but outcome remains unchanged ← **BUG MANIFESTS HERE**

#### Code Comments Mislead
The comment above the filter (lines 500-501) states:
```
"Only record attendees with something new either a first outcome or changed notes."
```

This suggests the code should check for "first outcome OR changed notes", but the implementation only checks for "changed notes", completely ignoring outcome changes.

---

### 3. Impact Analysis

#### Affected Workflows
1. **Initial Attendance Recording**: ✓ Works (first time is included automatically)
2. **Correcting Attendance Outcome**: ✗ BROKEN (outcome changes are filtered out)
3. **Adding Notes After Initial Recording**: ✓ Works (notes check catches this)
4. **Updating Both Outcome and Notes**: ✓ Works (notes change triggers inclusion)

#### User Impact
- Users see a success message when changing outcomes (misleading feedback)
- The outcome change is not persisted
- Session data in nDelius is not updated
- Audit trail shows the attempt but no actual change occurred
- Training/compliance records become inaccurate

#### Integration Impact
- nDelius appointments are not updated with new outcomes
- Referral status calculations may be based on stale attendance data
- Completion events for post-programme reviews may not trigger correctly

---

### 4. Root Cause

The filter logic is incomplete. It only considers notes changes and fails to account for outcome changes. The condition should use OR logic instead of only checking notes.

**Current Logic** (WRONG):
```kotlin
!submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes
```
- Only includes if notes changed
- Ignores outcome changes

**Required Logic** (CORRECT):
```kotlin
// Include if either:
// 1. Outcome changed, OR
// 2. Notes changed (and not empty)

val outcomeChanged = attendee.outcomeCode != latestAttendance.outcomeType.code
val notesChanged = !submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes

outcomeChanged || notesChanged
```

---

### 5. Test Coverage Gap

The existing test suite does not have a test case that specifically covers:
- Recording attendance with only an outcome change (no notes)
- Subsequent update to change just the outcome

This is why the bug wasn't caught during development.

**Current Tests**: 
- ✓ First attendance recording with notes
- ✓ Posting attendance for deleted membership
- ✓ Invalid referral validation
- ✗ Missing: Outcome-only change test

---

## Solution

### Fix Implementation

**File**: `src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SessionService.kt`

**Method**: `saveSessionAttendance()`

**Lines to Change**: 502-507

#### Current Code (Buggy)
```kotlin
val changedAttendees = sessionAttendance.attendees.filter { attendee ->
  val latestAttendance = latestAttendanceByReferralId[attendee.referralId] ?: return@filter true
  val submittedNotes = attendee.sessionNotes?.trim()
  val latestNotes = latestAttendance.notesHistory.maxByOrNull { it.createdAt }?.notes?.trim()
  !submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes
}
```

#### Fixed Code
```kotlin
val changedAttendees = sessionAttendance.attendees.filter { attendee ->
  val latestAttendance = latestAttendanceByReferralId[attendee.referralId] ?: return@filter true
  
  // Check if outcome has changed
  val outcomeChanged = attendee.outcomeCode != latestAttendance.outcomeType.code
  
  // Check if notes have changed (if new notes are provided)
  val submittedNotes = attendee.sessionNotes?.trim()
  val latestNotes = latestAttendance.notesHistory.maxByOrNull { it.createdAt }?.notes?.trim()
  val notesChanged = !submittedNotes.isNullOrEmpty() && submittedNotes != latestNotes
  
  // Include if either outcome or notes have changed
  outcomeChanged || notesChanged
}
```

### Explanation of Fix

1. **Outcome Change Detection**
   - Compares submitted `outcomeCode` with the latest recorded `outcomeType.code`
   - If different → outcome has changed

2. **Notes Change Detection** (unchanged from current logic)
   - Compares submitted notes with latest notes in history
   - Only counts as changed if new notes are non-empty and different

3. **Inclusion Logic**
   - Uses OR operator: `outcomeChanged || notesChanged`
   - Attendee is included if EITHER condition is true
   - Fixes the bug while maintaining the existing notes logic

### Why This Fix Is Correct

1. **Aligns with intended behavior**: Comment says "first outcome or changed notes" - this fix implements that
2. **Maintains backward compatibility**: Notes logic unchanged, outcome check added
3. **Preserves nDelius protection**: Still prevents duplicate updates for unchanged notes
4. **Minimal change**: Only adds outcome check, doesn't refactor the whole method
5. **Business logic sound**: Outcomes and notes are independent attributes that can change separately

---

## Testing Strategy

### Test Cases to Add

**Test 1: Record attendance with outcome only (no notes)**
```kotlin
@Test
fun `should record attendance when only outcome changes without notes`() {
  // Given: Previous attendance with outcome ATTC and no notes
  // When: Submit new outcome UAAB with no notes
  // Then: Attendance should be saved with new outcome UAAB
  //       nDelius should be updated
  //       Database should show outcome changed
}
```

**Test 2: Change outcome multiple times**
```kotlin
@Test
fun `should allow changing outcome multiple times without notes`() {
  // Given: Previous attendance with outcome ATTC
  // When: Submit UAAB, then AFTC, then UAAB again
  // Then: Each change should be persisted
  //       Each should create new SessionAttendanceEntity
  //       nDelius should be updated for each change
}
```

**Test 3: Outcome change takes precedence with notes**
```kotlin
@Test
fun `should update outcome and notes when both change`() {
  // Given: Previous attendance with outcome ATTC and notes "Old"
  // When: Submit outcome UAAB and notes "New"
  // Then: Both should be updated
  //       Should appear as single attendance record
}
```

### Existing Tests to Review
- Verify existing tests still pass with the fix
- Particularly: `SessionServiceTest.saveSessionAttendance*` tests
- Integration tests in `SessionControllerIntegrationTest`

---

## Deployment Notes

1. **Database Migration**: Not required - only logic change
2. **Backwards Compatibility**: ✓ Fully compatible - only adds missing functionality
3. **Feature Flag**: Not needed - this is a bug fix
4. **Rollback Plan**: Revert the single change in SessionService.kt
5. **Monitoring**: Watch logs for:
   - Successful attendance updates
   - nDelius update frequency
   - SessionAttendanceEntity creation rate

---

## Files to Modify

1. **Primary Fix**
   - `src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SessionService.kt`
     - Method: `saveSessionAttendance()` (lines 502-507)

2. **Tests to Add**
   - `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/service/SessionServiceTest.kt`
     - Add 3 new test cases for outcome-only changes

3. **Optional: Integration Tests**
   - `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/controller/SessionControllerIntegrationTest.kt`
     - Add integration test for end-to-end outcome change

---

## Summary

| Aspect | Details |
|--------|---------|
| **Bug Type** | Logic error in filtering changed records |
| **Affected Feature** | Attendance outcome recording |
| **Impact Level** | HIGH - Core functionality broken |
| **Line of Code** | SessionService.kt, lines 502-507 |
| **Fix Complexity** | LOW - 3 line addition + clarification |
| **Testing Impact** | LOW - No data migration, fully backward compatible |
| **User-Facing** | YES - Users cannot change outcomes without adding notes |
| **nDelius Impact** | YES - nDelius not updated when outcome-only changes submitted |

---

## Validation Checklist

- [ ] Code change applied to SessionService.kt
- [ ] New tests written and passing
- [ ] Existing tests still pass
- [ ] Manual testing: Record initial outcome
- [ ] Manual testing: Change outcome without notes
- [ ] Manual testing: Verify database shows new outcome
- [ ] Manual testing: Verify nDelius mock receives update
- [ ] Code review completed
- [ ] Deployed to dev environment
- [ ] Tested on dev by QA team
- [ ] Deployed to preprod/prod


