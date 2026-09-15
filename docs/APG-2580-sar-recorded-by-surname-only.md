# APG-2580 — SAR "Recorded by" surname-only fix (design & validation)

**Branch:** `APG-2580/sar-recorded-by-surname-only`
**Ticket:** APG-2580 (SAR round-2 QAT follow-ups) — staying on this ticket, no sub-ticket. Same-branch continuation of `APG-2580/entity-set-iteration-audit`.
**Status:** 🟡 Design / validation — **no code changes yet**. Sign off this doc before implementing.
**Companion doc (deferred follow-up):** [`APG-2580-sar-facilitator-personName-to-surname-rename-design.md`](./APG-2580-sar-facilitator-personName-to-surname-rename-design.md)

## 1. Context

Round-2 QAT reviewer email (Sep 2026) flagged that *"Recorded by"* fields in the SAR report for **Accredited Programmes Community** are returned as **full names** (e.g. `Joe Bloggs`) rather than the agreed **surname only** (e.g. `Bloggs`).

Data-dictionary alignment already confirmed (14 Sep 2026): the fresh CSV export of the *"Accredited Programmes Community"* tab is byte-identical to our stored round-2 render apart from one cosmetic date-format cell — see [`sar-data-dictionary/README.md`](./sar-data-dictionary/README.md).

## 2. Exhaustive audit — every string field in every SAR DTO

Every `String`-valued field emitted by `SubjectAccessRequestService.getProbationContentFor(...)` was traced back to the code that populates it. **Only the two fields highlighted in bold** carry a full name; every other `*By` / `*name` field is either a username, an enum label, an nDelius location string, or free-text.

| SAR DTO field (JSON path) | Populated from | Content type | Full name? |
|---|---|---|---|
| `accreditedProgrammeTemplate.name` | `AccreditedProgrammeTemplateEntity.name` | Programme title | ❌ |
| `availability.otherDetails` | entity | Free-text | ❌ |
| `availability.lastModifiedBy` | `AvailabilityService.getAuthenticatedReferrerUser()` = `SecurityContextHolder.authentication.name` (Spring `Authentication.getName()` returns the principal name = **username**) | Username | ❌ |
| `availability.slots[].dayOfWeek` / `slotName` | enum | Enum label | ❌ |
| `deliveryLocationPreference.createdBy` | `DeliveryLocationPreferenceEntity.createdBy` default = `SecurityContextHolder.authentication.name` | Username | ❌ |
| `deliveryLocationPreference.locationCannotAttendText` | entity | Free-text | ❌ |
| `deliveryLocationPreference.preferredDeliveryLocations[].deliusDescription` (+ PDU) | entity (nDelius sync) | Location text | ❌ |
| `messageHistory[].description` / `message` | entity | Event text / JSON payload | ❌ |
| **`programmeGroupMembership[].attendances[].recordedByFacilitator.personName`** | `FacilitatorEntity.personName` ← `CreateGroupTeamMember.facilitator` (client request, `@Schema("The full name of the facilitator for the group")`) | **FULL NAME** | ✅ **FIX #2** |
| `programmeGroupMembership[].attendances[].sessionId` / `groupMembershipId` | UUIDs | UUID | ❌ |
| `programmeGroupMembership[].attendances[].outcomeType.outcomeDescription` | enum | Enum label | ❌ |
| `programmeGroupMembership[].attendances[].legitimateAbsence` | entity | Enum-as-string | ❌ |
| **`programmeGroupMembership[].attendances[].noteHistory[].recordedBy`** | `SessionNotesHistoryEntity.createdByFullName` ← `userService.getUserByUsernameOrNull(createdBy)?.name` — `User.name` is the display name returned by Manage Users API (e.g. `"John Smith"`) | **FULL NAME** | ✅ **FIX #1** |
| `programmeGroupMembership[].attendances[].noteHistory[].note` | entity | Free-text | ❌ |
| `programmeGroupMembership[].createdByUsername` / `deletedByUsername` | entity (field name explicit) | Username | ❌ |
| `programmeGroupMembership[].programmeGroup.accreditedProgrammeTemplate.name` | entity | Programme title | ❌ |
| `referral.sex` / `interventionName` / `interventionType` / `setting` / `sourcedFrom` | entity / enums | Data / labels | ❌ |
| `referral.referralCohortHistories[].createdBy` | `CohortService.createdBy` param (username) or hard-coded `"SYSTEM"` / `"SEEDING_SYSTEM"` | Username / system tag | ❌ |
| `referral.referralCohortHistories[].cohort` | enum `displayName` | Enum label | ❌ |
| `referral.referralLdcHistories[].createdBy` | entity default = `SecurityContextHolder.authentication.name` | Username | ❌ |
| `referral.referralMotivationBackgroundAndNonAssociation.createdBy` | entity default = `SecurityContextHolder.authentication.name` | Username | ❌ |
| `referral.referralMotivationBackgroundAndNonAssociation.lastUpdatedBy` | `GroupAllocationNotesService.updatedBy` param (username) | Username | ❌ |
| `referral.referralMotivationBackgroundAndNonAssociation.motivation` / `nonAssociation` / `otherConsideration` | entity | Free-text | ❌ |
| `referral.referralReportingLocation.regionName` / `pduName` / `reportingTeam` | nDelius sync (`NDeliusPersonalDetails.probationDeliveryUnit.description` etc.) | Location text | ❌ |
| `referral.statusHistories[].createdBy` | `ReferralStatusHistoryEntity.createdBy` (**username** — entity also holds `createdByFullName` but the SAR mapper explicitly reads `createdBy`, not the full-name column) | Username | ❌ |
| `referral.statusHistories[].additionalDetails` | entity | Free-text | ❌ |
| `referral.statusHistories[].referralStatusDescription.description` | entity | Status label | ❌ |
| `referral.attendees[].session.createdByUsername` | entity (field name explicit) | Username | ❌ |
| `referral.attendees[].session.locationName` | entity | Location text | ❌ |
| `referral.attendees[].session.module.name` / `moduleSessionTemplate.*` | template entity | Programme text | ❌ |
| `referral.attendees[].session.sessionFacilitators[].facilitator.personName` | Same `FacilitatorEntity.personName` as fix #2 above, but rendered under the **Facilitators** section, **not** a "Recorded by" field | ⚠ FULL NAME but **out of scope** (see §2.1) | ❌ (not flagged by reviewer) |

### 2.1 The one deliberate carve-out — Facilitators list

`SubjectAccessRequestFacilitator.personName` is shared by **two** SAR JSON paths:

1. `...attendances[].recordedByFacilitator.personName` — rendered under **"Recorded by"** in the SAR PDF. ✅ **In scope.**
2. `...session.sessionFacilitators[].facilitator.personName` — rendered under the **"Facilitators"** section (the roster of who runs each session). Reviewer **did not flag** this; full name is the appropriate value there.

**Because the two paths share the DTO mapper `FacilitatorEntity.toApi()`, we must NOT apply the surname transformation in that mapper** — that would also strip names from the Facilitators section. Fix has to be at the call site (§4.3).

### 2.2 Answers to the previous revision's open questions

| Question | Answer |
|----------|--------|
| Are the two fields listed the complete set? | ✅ **Confirmed** by the audit above — every other SAR string field is a username, enum label, location string, free-text, UUID, or programme metadata. |
| Compound surnames — `van der Berg` → `Berg` or `van der Berg`? | ✅ **Keep as `van der Berg`.** Helper must preserve surname particles. |
| Rename `SubjectAccessRequestFacilitator.personName → surname`? | ⏭ **Defer** — separate follow-up branch. Design captured in the companion doc linked above. |
| Ticket / branch | ✅ Stay on APG-2580 / current branch. |

## 3. Scope

| # | SAR JSON path (relative to a referral) | Current | Desired |
|---|---------------------------------------|---------|---------|
| 1 | `programmeGroupMemberships[].attendances[].noteHistory[].recordedBy` | Full name (`Joe Bloggs`) | Surname only, particles preserved (`Bloggs`, `van der Berg`) |
| 2 | `programmeGroupMemberships[].attendances[].recordedByFacilitator.personName` | Full name (`Joe Bloggs`) | Surname only, particles preserved (`Bloggs`, `van der Berg`) |

**Out of scope:** everything else in §2, including the Facilitators-list carve-out in §2.1 and all Custody-side SAR endpoints (sibling repo).

## 4. Chosen approach

> **Repo convention re-check (15 Sep 2026):** the repo has no `service/util/`
> package. Reusable helpers live under the top-level `utils/` package
> (`AuthenticationUtils.kt`, `FormatTimeForUiDisplay.kt`, `ReferralStatusUtils.kt`,
> `SessionNameFormatter.kt`, `EmptyStringToNullDeserializer.kt`). Following that
> convention, the helper below is placed there.
>
> The `Entity.toApi()` extension functions for SAR **always** live in the SAR
> DTO file, never in the entity file (verified: all 20 existing SAR mappers
> follow this pattern — grep for `^fun.*Entity.toApi` under
> `api/model/subjectAccessRequest/`). The new `toSarRecordedByApi()` therefore
> co-locates with the existing `toApi()` in `SubjectAccessRequestFacilitator.kt`.

### 4.1 Helper

New file `src/main/kotlin/.../utils/NameFormatting.kt`:

```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

/**
 * Recognised lower-case surname particles ("nobiliary particles"). When one of
 * these appears immediately before the last uppercase-leading token, it is
 * treated as part of the surname (e.g. `van der Berg` -> `van der Berg`).
 *
 * Case-insensitive match. Kept small and explicit rather than "any lowercase
 * token" to avoid false positives on data-entry mistakes such as a lower-cased
 * forename ("joe Bloggs" -> still returns `Bloggs`).
 */
private val SURNAME_PARTICLES = setOf(
  "van", "von", "der", "den", "de", "del", "della", "di", "da", "dos",
  "du", "la", "le", "ten", "ter", "zu", "af", "bin", "ibn", "al", "el",
)

/**
 * Returns the surname portion of a full name for SAR output, complying with
 * the data-dictionary rule agreed in the round-2 QAT review (Sep 2026):
 *   > "surname only"  with compound surnames such as `van der Berg` preserved.
 *
 * Rules:
 *   - null / blank input           -> null
 *   - single-token input           -> returned unchanged (already a surname)
 *   - multi-token input            -> last token, PLUS any preceding tokens
 *                                     that match SURNAME_PARTICLES
 *   - casing preserved
 *   - leading / trailing / repeated whitespace tolerated
 */
fun toSurname(fullName: String?): String? {
  val trimmed = fullName?.trim().orEmpty()
  if (trimmed.isEmpty()) return null
  val tokens = trimmed.split(Regex("\\s+"))
  if (tokens.size == 1) return tokens[0]
  var startIndex = tokens.lastIndex
  while (startIndex > 0 && tokens[startIndex - 1].lowercase() in SURNAME_PARTICLES) {
    startIndex--
  }
  return tokens.subList(startIndex, tokens.size).joinToString(" ")
}
```

### 4.2 Wire-in — SessionNoteHistory (mapper is safe: only one JSON path)

`SubjectAccessRequestSessionNoteHistory.kt`:

```kotlin
// before
recordedBy = createdByFullName,
// after
recordedBy = toSurname(createdByFullName),
```

### 4.3 Wire-in — recordedByFacilitator (call site, NOT the shared mapper — see §2.1)

Add a SAR-specific projection alongside the existing `toApi()`. Following repo
convention this goes in the DTO file, not the entity.

`SubjectAccessRequestFacilitator.kt` (add below the existing `toApi()`):

```kotlin
/** SAR-specific projection that surname-only's the person name; see
 *  docs/APG-2580-sar-recorded-by-surname-only.md for rationale.
 *  Falls back to the full name if `toSurname()` returns null so we never
 *  emit a null personName into an otherwise non-nullable field. */
fun FacilitatorEntity.toSarRecordedByApi(): SubjectAccessRequestFacilitator =
  SubjectAccessRequestFacilitator(personName = toSurname(personName) ?: personName)
```

(Requires `import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.toSurname`.)

`SubjectAccessRequestSessionAttendance.kt`:

```kotlin
// before
recordedByFacilitator = recordedByFacilitator?.toApi(),
// after
recordedByFacilitator = recordedByFacilitator?.toSarRecordedByApi(),
```

**Rejected alternative:** inlining the transformation inside `SessionAttendance.toApi()` — surname logic would live in two places; a shared helper is cleaner.

### 4.4 Regression guard

`Session.sessionFacilitators[].facilitator.toApi()` continues to use the plain mapper → `personName` returned as stored (full name). Verified: `Facilitator`'s plain `toApi()` is only referenced from two call sites — `SubjectAccessRequestSessionAttendance.toApi()` (we change to use the new projection) and `SubjectAccessRequestSessionFacilitator.toApi()` (we leave alone).

## 5. Edge cases the helper must cover

| Input | Expected | Rationale |
|-------|----------|-----------|
| `"Joe Bloggs"` | `"Bloggs"` | Standard case. |
| `"joe bloggs"` | `"bloggs"` | Casing preserved. |
| `"Jean-Paul Sartre"` | `"Sartre"` | Hyphenated forename, not a particle. |
| `"Anne-Marie O'Brien"` | `"O'Brien"` | Apostrophe surname. |
| `"John van der Berg"` | `"van der Berg"` | Two particles preserved. ✅ **§2.2 answer.** |
| `"Maria de la Cruz"` | `"de la Cruz"` | Spanish/Portuguese compound. |
| `"Ludwig von Beethoven"` | `"von Beethoven"` | German particle. |
| `"Dr Sarah Hughes"` | `"Hughes"` | Title is uppercase → not a particle → drops off. |
| `"Bloggs"` | `"Bloggs"` | Single token. |
| `"  Joe   Bloggs  "` | `"Bloggs"` | Whitespace tolerance. |
| `null` | `null` | Nullable input. |
| `""` / `"   "` | `null` | Empty / whitespace-only treated as absent. |
| `"VAN DER BERG"` | `"VAN DER BERG"` | All-caps: particle match is case-insensitive; walking left both preceding tokens match and we stop at index 0, returning the whole string. Sensible fallback. |

## 6. Test plan

> **Repo test conventions (verified 15 Sep 2026):** JUnit 5 + mockk + AssertJ
> `assertThat` + kotest assertions. Tests live in `src/test/kotlin/.../<same package>/<Class>Test.kt`.
> Methods named with backticks describing behaviour. Entity factories under
> `src/test/kotlin/.../factory/` (e.g. `SessionNotesHistoryEntityFactory`).
>
> **Current SAR mapper test coverage: zero.** A grep for
> `SubjectAccessRequestSessionNoteHistory`, `SubjectAccessRequestFacilitator`
> and `SubjectAccessRequestSessionAttendance` in `src/test/kotlin` returned no
> hits — no existing assertions need updating. Everything below is net-new.

1. **New unit tests** — `src/test/kotlin/.../utils/NameFormattingTest.kt` covering every row in §5. Follow the existing test-style (see `AvailabilityServiceTest`): JUnit 5, AssertJ `assertThat`, backticked method names.
2. **New SAR mapper tests** — one focused test per changed mapper, using the existing entity factories:
   - `SessionNotesHistoryEntity(createdByFullName = "John van der Berg").toApi().recordedBy` must equal `"van der Berg"`.
   - `SessionAttendanceEntity(recordedByFacilitator = FacilitatorEntity(personName = "Joe Bloggs")).toApi().recordedByFacilitator?.personName` must equal `"Bloggs"`.
   - **Regression guard:** `SessionFacilitatorEntity(...facilitator = FacilitatorEntity(personName = "Joe Bloggs")).toApi().facilitator.personName` must still equal `"Joe Bloggs"` (full name).
3. **`SubjectAccessRequestServiceTest`** — the existing 296-line test asserts on filtering and counts, not field values. Add one end-to-end assertion that a produced `SubjectAccessRequestContent` surfaces the surname-only values through the full chain.
4. **Non-SAR tests unchanged** — nothing on the `SessionNotes` UI path, case-list, or session-facilitator edit paths should change; helper is only invoked in the two SAR spots.
5. **Manual smoke** — hit `/subject-access-request?crn=<seed CRN>` in dev after deploy, grep response JSON:
   - `recordedBy` values only contain the last-name token (or particle+lastname compound).
   - `recordedByFacilitator.personName` same.
   - `sessionFacilitators[].facilitator.personName` still shows full names.

## 7. Documentation updates in the same PR

- KDoc on `SubjectAccessRequestSessionNoteHistory.recordedBy` — note "surname only, particles preserved".
- KDoc on the new `FacilitatorEntity.toSarRecordedByApi()` cross-linking this design doc.
- SAR OpenAPI descriptions on the two fields updated to say "surname only".
- New `docs/schema-docs/2026-09-XX-sar-recorded-by-surname-only.md` entry (post-merge shape change).

## 8. Rollout / risk

| Risk | Mitigation |
|------|-----------|
| SAR consumers depend on full-name shape | Low — reviewer explicitly asked for surname; SAR collator renders whatever we send. |
| Regression on Facilitators list (§2.1) | Guarded by §4.4 code structure + explicit test (§6 item 2c). |
| Compound-surname edge case differs from reviewer expectation | Particle set (§4.1) is centralised + unit-tested; reviewer sign-off requested in §9. |
| Deployment | API-only, no DB migration, no schema break — safe under normal CI/CD. |

## 9. Validation checklist (sign off before writing code)

- [x] Two fields = complete set — corroborated by exhaustive audit (§2).
- [x] Compound-surname behaviour = **preserve particles** (`van der Berg`).
- [x] Rename `personName → surname` **deferred** — separate design doc created.
- [x] Ticket / branch — stay on APG-2580 / this branch.
- [ ] Reviewer confirms Facilitators-list carve-out (§2.1) — only the two "Recorded by" fields to change, Facilitators section left as full name.
- [ ] Reviewer confirms `SURNAME_PARTICLES` set (§4.1) is acceptable, or supplies a preferred list.

## 10. Implementation checklist (once §9 is signed off)

- [ ] Add `src/main/kotlin/.../utils/NameFormatting.kt` with `toSurname()` + KDoc (§4.1).
- [ ] Add `FacilitatorEntity.toSarRecordedByApi()` extension inside `api/model/subjectAccessRequest/SubjectAccessRequestFacilitator.kt` (§4.3).
- [ ] Update `SubjectAccessRequestSessionNoteHistory.toApi()` (§4.2).
- [ ] Update `SubjectAccessRequestSessionAttendance.toApi()` (§4.3).
- [ ] Add `src/test/kotlin/.../utils/NameFormattingTest.kt` covering §5.
- [ ] Add SAR mapper tests + facilitators-list regression assertion (§6 items 2–3).
- [ ] KDoc + OpenAPI description updates (§7).
- [ ] Add `docs/schema-docs/2026-09-XX-sar-recorded-by-surname-only.md`.
- [ ] `./gradlew check` locally, push, raise PR linking this doc.

## 11. Verification log (rev-3, 15 Sep 2026)

Every claim in this document was re-verified by reading the following files
directly on branch `APG-2580/sar-recorded-by-surname-only` at HEAD `22c3e298`:

| Verified | Evidence |
|----------|----------|
| Helper package convention | `ls src/main/kotlin/.../utils/` — 5 existing helper files; no `service/util/` directory exists. |
| SAR `toApi()` extension location | `grep '^fun.*Entity.toApi' api/model/subjectAccessRequest/*.kt` — 20 hits, all in DTO files. |
| `Facilitator.toApi()` caller count = 2 | `grep 'facilitator.*\.toApi'` → `SubjectAccessRequestSessionAttendance.kt:25`, `SubjectAccessRequestSessionFacilitator.kt:10`. Zero other hits. |
| `SessionNotesHistoryEntity.createdByFullName` type | `entity/SessionNotesHistoryEntity.kt:49` — `var createdByFullName: String? = null`. Nullable. |
| `FacilitatorEntity.personName` type | `entity/FacilitatorEntity.kt:22` — `@NotNull var personName: String`. Non-null. |
| `authentication?.name` returns username in HMPPS Auth | `utils/AuthenticationUtils.kt` explicitly wraps `HmppsAuthenticationHolder.username`; `AvailabilityServiceTest.kt` mocks `authentication.name` as `"test-user"`. |
| `User.name` = display name from Manage Users API | `model/User.kt` has `username: String, active: Boolean, name: String` — three distinct fields; SAR path (`ReferralService.kt:628-636`) reads `.name` after `getUserByUsernameOrNull(createdBy)`. |
| `CreateGroupTeamMember.facilitator` is documented as a full name | `api/model/programmeGroup/CreateGroupRequest.kt` — `@Schema(description = "The full name of the facilitator for the group")`. |
| No existing tests assert on the two DTO fields | `grep 'SubjectAccessRequestSessionNoteHistory\|SubjectAccessRequestFacilitator\|SubjectAccessRequestSessionAttendance' src/test/kotlin` → **zero hits**. |
| Test framework in use | `build.gradle.kts` — JUnit 5 (`vintage` excluded), `mockk`, `kotest-assertions-core`, `assertj` via HMPPS starter. |
| Compound-surname algorithm produces every value in §5 | Hand-traced all 13 rows; results match the "Expected" column. |

## 12. Change log

- **rev-1** (14 Sep): initial design; assumed `service/util/` package and `FacilitatorEntity.kt`-hosted extension; asked open questions about scope, particles, rename.
- **rev-2** (14 Sep): after exhaustive audit — confirmed only two full-name fields; corrected the plan to surname at the call site (not the shared Facilitator mapper) so the Facilitators list is preserved; §2 audit table added; the rename split off into a companion doc.
- **rev-3** (15 Sep): full inline code + repo-convention re-verification. Corrected helper package to `utils/` (not `service.util/`), moved `toSarRecordedByApi()` into the DTO file (repo convention), rewrote §6 to reflect the "zero existing SAR mapper tests" fact. Added §11 verification log and §12 change log.
