# APG-2580 follow-up — rename `SubjectAccessRequestFacilitator.personName` → `surname` (design)

**Status:** ⏸ **Deferred** — not being implemented in the current branch. This doc captures the plan so we can pick it up cleanly on a future branch once the immediate surname-only value fix ([`APG-2580-sar-recorded-by-surname-only.md`](./APG-2580-sar-recorded-by-surname-only.md)) is merged and the reviewer has validated.
**Suggested future branch:** `APG-2580/sar-facilitator-personName-to-surname-rename`
**Depends on:** `APG-2580/sar-recorded-by-surname-only` (must be merged first).

## 1. Why this is a separate branch

The immediate fix changes the **value** returned for two SAR fields (full name → surname). This follow-up changes the **field name** in the JSON schema itself:

```
SubjectAccessRequestFacilitator.personName  →  SubjectAccessRequestFacilitator.surname
```

That is a **breaking change to the SAR JSON schema** and needs coordinated sign-off from the SAR collator team and the reviewer, so we deliberately separate the concerns:

| Concern | Where |
|---------|-------|
| Fix the value shown in the report (unblocks retesting) | Current branch — additive, no schema break. |
| Rename field so the schema reads correctly (`surname` instead of `personName`) | This deferred branch — schema break, needs coordination. |

## 2. Scope

Only the SAR side. Inside this repo the `personName` label lives in many places (case list, session facilitator edit, seeding, referral model, etc.); those must stay unchanged. The rename is limited to the SAR DTO exposed by `getProbationContentFor(...)`.

| # | Change | File | Type |
|---|--------|------|------|
| 1 | Rename data-class field | `api/model/subjectAccessRequest/SubjectAccessRequestFacilitator.kt` | Breaking (schema) |
| 2 | Update `FacilitatorEntity.toSarRecordedByApi()` (co-located in the same DTO file per repo convention) to write `surname = ...` | `api/model/subjectAccessRequest/SubjectAccessRequestFacilitator.kt` | Trivial |
| 3 | Update the roster mapper — see §3 (Option A splits it into a separate DTO) | `api/model/subjectAccessRequest/SubjectAccessRequestFacilitator.kt` (or new file) | Design decision |
| 4 | Update OpenAPI schema description | Same file / annotation | Docs |
| 5 | Update any SAR mapper tests referencing the old key | `src/test/kotlin/.../api/model/subjectAccessRequest/...` | Test-only |

## 3. Design decision — what happens to the Facilitators list?

`SubjectAccessRequestFacilitator` is shared between two JSON paths (see §2.1 of the immediate-fix doc):

- **Recorded by** — post-immediate-fix carries a surname value.
- **Facilitators list** — still carries a full name value (deliberately).

If we rename the field to `surname`, the Facilitators list would then have `surname: "Joe Bloggs"` in its JSON — misleading, since `Joe Bloggs` isn't a surname.

**Two options** for handling this at rename time; pick before implementing:

### Option A — split the DTO into two types (recommended)

```kotlin
// New: dedicated recorded-by projection
data class SubjectAccessRequestRecordedByFacilitator(val surname: String)

// Existing: keep for the facilitators roster, rename field to reflect content
data class SubjectAccessRequestFacilitatorRosterEntry(val personName: String)
```

- **Pro:** each field name honestly describes its content; consumers can't misuse it.
- **Con:** two mapper functions; consumers of the SAR JSON schema see two types where they had one.

### Option B — rename to `surname` universally and accept the mis-labelling on the roster

- **Pro:** minimal change.
- **Con:** roster entries labelled `surname` while actually containing full names is confusing and undermines the whole point of the rename.

**Recommendation:** Option A. Ships a cleaner schema and makes the intent obvious in every consumer's IDE.

## 4. Coordination checklist (before starting the rename branch)

- [ ] SAR collator team confirms it can accept a schema change on this endpoint (breaking change to the SAR JSON contract).
- [ ] Reviewer signs off on the rename (should be trivially yes once the surname value is agreed).
- [ ] Option A vs Option B decided.
- [ ] Immediate-fix PR (`APG-2580/sar-recorded-by-surname-only`) has merged and been running clean in dev for ≥ 1 sprint.
- [ ] Schema-docs entry drafted describing the new shape.

## 5. Implementation plan (once §4 is signed off)

Assuming Option A:

1. Branch from latest `main`: `git checkout -b APG-2580/sar-facilitator-personName-to-surname-rename`.
2. Add `SubjectAccessRequestRecordedByFacilitator(surname: String)`.
3. Rename existing `SubjectAccessRequestFacilitator` → `SubjectAccessRequestFacilitatorRosterEntry` (or similar); IntelliJ safe-rename handles imports.
4. Replace the current `FacilitatorEntity.toSarRecordedByApi(): SubjectAccessRequestFacilitator` (added by the immediate fix) with `FacilitatorEntity.toSarRecordedByApi(): SubjectAccessRequestRecordedByFacilitator`.
5. Update `SubjectAccessRequestSessionAttendance` to use the new type.
6. Update `SubjectAccessRequestSessionFacilitator` to use `SubjectAccessRequestFacilitatorRosterEntry`.
7. Update SAR mapper tests to assert new key names.
8. Update OpenAPI schema descriptions + example values.
9. Add `docs/schema-docs/YYYY-MM-XX-sar-facilitator-schema-rename.md` explaining the shape change.
10. `./gradlew check`; raise PR; coordinate merge with the SAR team.

## 6. Rollout considerations

- **SAR collator** parses the JSON by field name — updating the collator template is mandatory alongside this rename.
- **Backfill / retro reports** — SAR content is generated on demand from the current DB, so no historical data migration is required.
- **Feature flag** — probably not needed; the SAR endpoint is behind service-to-service auth and used only by the collator, which we control.

## 7. Rollback plan

Revert commit + republish OpenAPI schema. Collator template can be reverted or kept — old-name and new-name mappings differ only by JSON key, so the collator team can dual-support during rollout if they prefer.

---
_Deferred design doc. Do not implement without coordination with the SAR team._
_Created 14 Sep 2026._

