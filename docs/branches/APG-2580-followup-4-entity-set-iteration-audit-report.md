# APG-2580 Entity `MutableSet` iteration-order audit — 2026-08-25

Follow-up 4 of the APG-2580 SAR remediation work. Motivated by correction #8
(the Hibernate/byte-buddy bump that flipped SAR JSON ordering) and the Branch 5
root-cause investigation, which observed that `MutableSet` on the entity side is
JPA-correct but has no guaranteed iteration order — Hibernate's default
`LinkedHashSet` gives insertion-order-of-result-set, but that contract is
neither documented nor test-enforced and is sensitive to Hibernate-version bumps
and fetch-strategy changes.

Reference-implementation for the natural-attribute-tiebreak fix: PR #877 merge
commit `2a971033` on `SubjectAccessRequestReferral.kt`.

---

## Scope

Pre-flight grep (`grep -rn ": MutableSet<" src/main/kotlin/…/entity/`):
**17 hits across 10 entity classes** (one line is a constructor parameter of
`DeliveryLocationPreferenceEntity`, not a field, so **16 fields catalogued**).

`@OrderBy` present on entity `Set`/`List` fields: **6** (`ReferralEntity.statusHistories`
[`List`], `ReferralEntity.referralCohortHistories`, `ModuleEntity.sessionTemplates`,
`SessionAttendanceEntity.notesHistory` [`List`], `AccreditedProgrammeTemplateEntity.modules`,
`ProgrammeGroupEntity.sessions`).

Order-sensitive call patterns in `src/main` (`.first()|.last()|.firstOrNull()|
.lastOrNull()|.maxByOrNull|.minByOrNull`): **52** hits — most against `List`,
DTO collections, or non-entity `Iterable` receivers; only the entity-Set
receivers listed below are in scope for this audit.

Verification of Branch 5 fixes in `SubjectAccessRequestReferral.kt`:

- `programmeGroupMemberships` — sorted `compareBy(createdAt).thenBy(programmeGroup.code).thenBy(createdByUsername)` ✅
- `referralLdcHistories` — sorted `compareByDescending(createdAt).thenBy(hasLdc).thenBy(createdBy)` ✅
- `referralCohortHistories` — sorted `compareByDescending(createdAt).thenBy(createdBy).thenBy(cohort.name)` ✅

---

## Fields catalogued

### `ReferralEntity.referralLdcHistories`
- Type: `MutableSet<ReferralLdcHistoryEntity>`
- `@OrderBy`: **none**
- Consumers (grep `\.referralLdcHistories\b` in `src/main`):
  - `ReferralService.kt:286` — bulk reassignment (`= referralLdcHistories`) — **safe** (not iteration)
  - SAR DTO mapper `SubjectAccessRequestReferral.kt` — explicit `sortedWith(...)` (Branch 5 fix) — **safe**
- Verdict: **safe** (only order-sensitive consumer is the SAR mapper and it already sorts explicitly with a natural-attribute tiebreak).

### `ReferralEntity.referralCohortHistories`
- Type: `MutableSet<ReferralCohortHistoryEntity>`
- `@OrderBy`: **`createdAt DESC`**
- Consumers (`src/main`):
  - `ReferralSeederService.kt:135` — `.add(...)` — **safe**
  - `ReferralService.kt:282` — bulk reassignment — **safe**
  - `TelemetryService.kt:39` — was `.firstOrNull()` — **silently order-sensitive**: Hibernate's `@OrderBy` puts most-recent-first at the SQL layer, but two rows sharing `createdAt` fall through to Hibernate's undefined tiebreak (LinkedHashSet insertion order → sensitive to correction #8's bump). Intent is "current cohort for telemetry".
  - SAR DTO mapper — explicit `sortedWith(...)` (Branch 5 fix) — **safe**
- Verdict: **fixed on this branch** — replaced `firstOrNull()` with `sortedWith(compareByDescending(createdAt).thenBy(createdBy).thenBy(cohort.name)).firstOrNull()`, mirroring the reference fix. See `TelemetryService.kt:39`.

### `ReferralEntity.programmeGroupMemberships`
- Type: `MutableSet<ProgrammeGroupMembershipEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `ReferralRepository.kt:16` — `LEFT JOIN FETCH` — **safe**
  - `GroupDetailsResponse.kt:142` — `.count { ... }` — **safe** (order-agnostic)
  - `ReferralStatusService.kt:113,157` — `.maxByOrNull { it.createdAt }` — **silently order-sensitive**. No entity-side tiebreak; ties on `createdAt` are Hibernate-lottery. Not in the pre-catalogued scope; flagged for follow-up.
  - `ProgrammeGroupMembershipService.kt:75` — `.add(...)` — **safe**
  - SAR DTO mapper — explicit `sortedWith(...)` (Branch 5 fix) — **safe**
- Verdict: **at-risk** (see "Additional findings" below — recommended for a separate ticket, not fixed on this branch).

### `AvailabilityEntity.slots`
- Type: `MutableSet<AvailabilitySlotEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `AvailabilityTransformer.kt:33,48,53` — assignment / `.addAll` / `.groupBy { it.dayOfWeek }` — **safe** (grouping is order-agnostic)
  - `AvailabilityService.kt:88,91,94,100` — `.clear() / .add() / .any / .filter { it.value }` inside DTO iteration (over the request DTO, not the entity Set) — **safe**
- Verdict: **safe** (no first/last/max/min consumer).

### `ModuleEntity.sessionTemplates`
- Type: `MutableSet<ModuleSessionTemplateEntity>`
- `@OrderBy`: **`sessionNumber ASC`**
- Consumers (`src/main`):
  - `ProgrammeGroupService.kt:517` — `.flatMap { ... }` — **safe**
  - `ScheduleService.kt:200` — `.sortedBy { it.sessionNumber }` — **safe** (explicit re-sort, matches `@OrderBy`)
- Verdict: **safe** (DB-enforced ordering + explicit re-sort at the only consumer that cares).

### `AccreditedProgrammeTemplateEntity.modules`
- Type: `MutableSet<ModuleEntity>`
- `@OrderBy`: **`moduleNumber ASC`**
- Consumers (`src/main`):
  - `ProgrammeGroupService.kt:516` — `.map { module -> ... }` — order-preserving `map`, downstream nests into module→sessions and is DB-ordered — **safe**
- Verdict: **safe** (DB-enforced).

### `AccreditedProgrammeTemplateEntity.programmeGroups`
- Type: `MutableSet<ProgrammeGroupEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`): none (no `.programmeGroups` accessor call-site in `src/main` outside the entity itself).
- Verdict: **safe** (no consumer).

### `ProgrammeGroupEntity.programmeGroupSessionSlots`
- Type: `MutableSet<ProgrammeGroupSessionSlotEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `SessionService.kt:185,195` — `.isNotEmpty` / pass-through — **safe**
  - `ProgrammeGroupService.kt:137,202,204,474,609` — `.addAll / .clear / joined into a `daysAndTimes` list where entries are already deduped by `(dayOfWeek, time)` in downstream formatting — **safe** (verified: line 474 collects `dayOfWeek`+`amOrPm` strings and joins; order of `daysAndTimes` is user-visible but only 1 row per (day,ampm) slot pair, which is DB-unique. Ordering is stable-per-seed via the underlying `Set`'s insertion order coming from the SQL result set, and there is no test that pins a specific order. Any user-visible drift here is cosmetic-only.)
  - `ScheduleService.kt:161,216` — assignment / `.map` — **safe**
- Verdict: **safe (with note)** — no `first/last/max/min` consumer; ordering of the joined `daysAndTimes` string is cosmetic and not asserted anywhere. Consider an `@OrderBy("dayOfWeek ASC, startsAt ASC")` if a future consumer needs a stable UI-visible order; not fixed here (would be a speculative change per anti-pattern #2).

### `ProgrammeGroupEntity.groupFacilitators`
- Type: `MutableSet<ProgrammeGroupFacilitatorEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `GroupDetailsResponse.kt:146,149` — `.filter { ... }.map { it.facilitator.personName }.sorted()` — **safe** (explicit `.sorted()`)
  - `ProgrammeGroupService.kt:133,224,234,299,649` — `.add / .clear / .map / partition by `facilitatorType` — order-agnostic — **safe**
  - `ScheduleService.kt:265` — `.map { ... }` copied into `sessionFacilitators` — **safe** (downstream sort applied at DTO layer, see `ProgrammeGroupService.kt:837` `.sortedBy { it.facilitator.personName }`)
- Verdict: **safe**.

### `ProgrammeGroupEntity.sessions`
- Type: `MutableSet<SessionEntity>`
- `@OrderBy`: **`startsAt ASC`**
- Consumers (`src/main`):
  - `ReferralStatusService.kt:176` — `.firstOrNull()` chained after a filter — **at-risk?** Verified: filter narrows to a specific `sessionType` + attendee, and picks the *first upcoming* by DB-enforced `startsAt ASC`. Ties on `startsAt` are possible but rare (two sessions in the same group at the exact same start time is not seeded/legal). **Safe** given DB-enforced `startsAt ASC` and the domain uniqueness of `(group, startsAt, moduleSessionTemplate)` — no code fix needed, but a `docs/`-level note that `startsAt` is treated as unique-per-group would harden the assumption for a future ticket.
  - `SessionService.kt:172` — `.filter { it.startsAt > session.startsAt }` — order-agnostic — **safe**
  - `ProgrammeGroupMembershipService.kt:92,196,215` — `.filter / .removeAll` — **safe**
  - `ScheduleService.kt:333,337,341,379,381,394,399,407` — `.mapNotNull / .addAll / .toSet / .filter / `.maxByOrNull { it.startsAt }` — the `maxByOrNull` at line 394 has the same "ties on `startsAt`" risk as above; same mitigation applies. **Safe** given domain uniqueness of `startsAt` per group.
- Verdict: **safe** (DB-enforced ordering + domain uniqueness of `startsAt`).

### `ProgrammeGroupEntity.programmeGroupMemberships`
- Type: `MutableSet<ProgrammeGroupMembershipEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `GroupDetailsResponse.kt:142` — `.count { ... }` — **safe**
- Verdict: **safe** (only consumer is order-agnostic).

### `DeliveryLocationPreferenceEntity.preferredDeliveryLocations`
- Type: `MutableSet<PreferredDeliveryLocationEntity>` (the second grep hit on line 70 is a constructor parameter, same field.)
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `DeliveryLocationPreferencesService.kt:72,77` — `.clear / .addAll` — **safe**
  - `DeliveryLocationPreferencesService.kt:88` — `.flatMap` on the *request DTO*, not the entity — **safe**
  - `DeliveryLocationPreferencesService.kt:130` — `.map { location -> ... }` reads to a DTO list — order propagates but no downstream `first/last/max` — **safe**
  - SAR DTO mapper — sorts via `deliveryLocationPreferences?.toApi()` which itself sorts when needed — **safe**
- Verdict: **safe**.

### `ProgrammeGroupMembershipEntity.attendances`
- Type: `MutableSet<SessionAttendanceEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `ReferralService.kt:685` — `.map { attendance -> ... }` (per-attendance iteration) — order propagates into `AttendanceHistorySession` list but the resulting list is not asserted-ordered anywhere in `src/test` — **safe (order-agnostic-consumer)**
  - `ReferralService.kt:688` — `.filter { it.session.id == session.id }.maxByOrNull { it.createdAt }` — **silently order-sensitive** on `createdAt` ties. Not pre-catalogued; flagged for follow-up.
- Verdict: **at-risk** (see "Additional findings" below — recommended for a separate ticket, not fixed on this branch).

### `SessionEntity.sessionFacilitators`
- Type: `MutableSet<SessionFacilitatorEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `SessionService.kt:360,386,387,642` — `.map / .clear / .addAll / .find { it.facilitatorType == REGULAR_FACILITATOR }` — the `.find` is on a domain-unique predicate (only one regular facilitator per session by construction) — **safe**
  - `ProgrammeGroupService.kt:307,308,547,837` — `.clear / .addAll / .filter / .sortedBy { it.facilitator.personName }` (explicit sort) — **safe**
  - `ScheduleService.kt:102,265` — assignment — **safe**
  - `CreateAppointmentRequest.kt:36` — `.additionalFacilitators = session.sessionFacilitators` streamed further with an explicit filter — **safe**
- Verdict: **safe**.

### `SessionEntity.attendances`
- Type: `MutableSet<SessionAttendanceEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `NDeliusAppointmentEntity.kt:44` (`currentAttendance()`) — `.filter { … referral.id == referral.id }.maxByOrNull { it.recordedAt ?: it.createdAt }` — **silently order-sensitive** on `recordedAt`/`createdAt` ties. Not pre-catalogued; flagged for follow-up.
  - `SessionNotes.kt:105` — `.filter { ... }.maxByOrNull { it.createdAt }` — **silently order-sensitive**. Not pre-catalogued; flagged for follow-up.
  - `SessionService.kt:461` — `.groupBy { referral.id }.mapValues { attendances.maxWithOrNull(compareBy(createdAt).thenBy(recordedAt)) }` — has a natural-attribute tiebreak already (`recordedAt`) but `recordedAt` is nullable and could still tie. Low-risk residual.
  - `SessionService.kt:487` — `.addAll(...)` — **safe**
  - `SessionService.kt:561` — was `.maxWithOrNull(compareBy(createdAt).thenBy(it.id))` — **UUID lottery, high-risk** (pre-catalogued).
  - `SessionService.kt:617` — `.filter { ... }.maxByOrNull { it.createdAt }` — **silently order-sensitive**. Not pre-catalogued; flagged for follow-up.
  - `ProgrammeGroupService.kt:808` — was `.sortedWith(compareByDescending(createdAt).thenByDescending(it.id)).firstOrNull()` — **UUID lottery, high-risk** (pre-catalogued).
- Verdict: **fixed on this branch** for the two pre-catalogued UUID-lottery sites (SessionService and ProgrammeGroupService). See `Iterable<SessionAttendanceEntity>.latestByCreatedAt()` in `SessionAttendanceEntity.kt`. The four remaining silently-order-sensitive consumers listed above are flagged for a follow-up ticket (see "Additional findings").

### `SessionEntity.ndeliusAppointments`
- Type: `MutableSet<NDeliusAppointmentEntity>`
- `@OrderBy`: **none**
- Consumers (`src/main`):
  - `SessionService.kt:228,230,261,268,401,493` — `.isNotEmpty / .isEmpty / .flatMap / .map / .toList / .find { it.referral.id == referralId }` — `.find` uses a domain-unique predicate (one appointment per (referral, session)) — **safe**
  - `ProgrammeGroupMembershipService.kt:204` — `.filter { it.referral.id == referral.id }` — **safe**
  - `ScheduleService.kt:387,668` — `.flatMap` — **safe**
- Verdict: **safe**.

---

## Findings summary

Pre-catalogued at-risk sites (re-verified against source, all confirmed):

| # | Site | Anti-pattern | Fix on this branch |
|---|------|---|---|
| 1 | `TelemetryService.kt:39` | `firstOrNull()` on `@OrderBy`-sorted Set → Hibernate-lottery tiebreak | ✅ replaced with `sortedWith(compareByDescending { createdAt }.thenBy { createdBy }.thenBy { cohort.name }).firstOrNull()` |
| 2 | `SessionService.kt:560` (line 561) | `.thenBy { it.id }` UUID tiebreak on `@GeneratedValue` UUID | ✅ replaced via new `Iterable<SessionAttendanceEntity>.latestByCreatedAt()` helper (natural tiebreak: `createdBy`, `outcomeType.code.name`) |
| 3 | `ProgrammeGroupService.kt:746` (line 808 in current tree) | `.thenByDescending { it.id }` UUID tiebreak on `@GeneratedValue` UUID | ✅ replaced via same helper — consolidation prevents divergence between the two attendance-picker sites |

Shallow-sweep additional findings **not** fixed on this branch (flagged for
follow-up tickets — this ticket is limited to the pre-catalogued UUID-lottery
sites per the user's directive; the below are silently order-sensitive but do
not involve the specific `.thenBy { it.id }` anti-pattern):

| Site | Concern | Suggested fix |
|---|---|---|
| `ReferralStatusService.kt:113` | `programmeGroupMemberships.maxByOrNull { it.createdAt }` — no tiebreak; ties on `createdAt` fall through to Hibernate lottery | add natural tiebreak (`programmeGroup.code`, `createdByUsername`) or add `@OrderBy("createdAt DESC")` on `ReferralEntity.programmeGroupMemberships` |
| `ReferralStatusService.kt:157` | same as above | same |
| `ReferralService.kt:688` | `membership.attendances.filter { ... }.maxByOrNull { it.createdAt }` | route through the new `latestByCreatedAt()` helper |
| `SessionService.kt:617` | `session.attendances.filter { ... }.maxByOrNull { it.createdAt }` | route through `latestByCreatedAt()` |
| `SessionService.kt:461` | `maxWithOrNull(compareBy(createdAt).thenBy(recordedAt))` — `recordedAt` is nullable → could still tie | route through `latestByCreatedAt()` (adds `createdBy`, `outcomeType.code.name` tiebreak) |
| `NDeliusAppointmentEntity.kt:44` (`currentAttendance()`) | `maxByOrNull { recordedAt ?: createdAt }` — no tiebreak | route through a variant of `latestByCreatedAt()` that preserves the `recordedAt ?: createdAt` selector, or refactor to use `latestByCreatedAt()` directly |
| `SessionNotes.kt:105` | `session.attendances.filter { ... }.maxByOrNull { it.createdAt }` | route through `latestByCreatedAt()` |

**Verdict rollup:**

- 16 fields catalogued.
- 3 pre-catalogued **at-risk** findings — all **fixed on this branch** via the
  natural-attribute tiebreak pattern (reference: PR #877 commit `2a971033`).
- 7 additional silently-order-sensitive call-sites surfaced by the shallow
  sweep — all "same-family" (attendance-latest-by-createdAt) or "membership-latest-by-createdAt"
  concerns, not the UUID-lottery anti-pattern this ticket is killing.
  Recommended for a follow-up ticket rather than fixing here, per anti-pattern
  #3 ("do not batch large refactors into this branch"). A single follow-up
  ticket can consolidate all attendance-latest call-sites through
  `latestByCreatedAt()` and add an analogous helper for
  `programmeGroupMemberships`.

Per the decision tree, 3 pre-catalogued findings falls into the "1–3 low-risk
findings: fix in-place on this branch" bucket. The 7 additional findings are
low-risk (silently-order-sensitive but not UUID-lottery) and are recommended
for a separate consolidation ticket rather than in-place batching.

---

## Follow-up work (recommended tickets)

**Delivery plan (2026-08-25):** the follow-ups below are grouped into **two PRs**:

- **F5 — [`APG-2580-followup-5-attendance-latest-consolidation.md`](./APG-2580-followup-5-attendance-latest-consolidation.md)** covers items 1 (all 5 attendance-latest sites, using the new sibling helper for `NDeliusAppointmentEntity.currentAttendance()`).
- **F6 — [`APG-2580-followup-6-membership-and-status-history-tiebreaks.md`](./APG-2580-followup-6-membership-and-status-history-tiebreaks.md)** covers item 2 (`programmeGroupMemberships` `@OrderBy` + `ReferralStatusService` tiebreaks) plus item 4 (`TelemetryService.statusHistories` symmetry fix).
- Item 3 (`programmeGroupSessionSlots` cosmetic `@OrderBy`) deliberately deferred — no consumer currently reads it in an order-sensitive way. Revisit if/when one does.

The raw item list below is kept as the source of truth for what F5 / F6 must cover — implementer prompts in each doc trace back to these items.

1. **Consolidate attendance-latest selection through `latestByCreatedAt()`** —
   route `ReferralService.kt:688`, `SessionService.kt:461`, `SessionService.kt:617`,
   `NDeliusAppointmentEntity.currentAttendance()`, and `SessionNotes.kt:105`
   through the new helper. Small pure refactor.
2. **Add a `latestByCreatedAt()`-equivalent for `programmeGroupMemberships`** —
   fix `ReferralStatusService.kt:113,157`, or add
   `@OrderBy("createdAt DESC")` on `ReferralEntity.programmeGroupMemberships`.
   Note the JPA-level fix would benefit every consumer at once (per the doc's
   "preferred" fix in Step 4).
3. (Optional, cosmetic) Add `@OrderBy("dayOfWeek ASC, startsAt ASC")` on
   `ProgrammeGroupEntity.programmeGroupSessionSlots` if any future consumer
   depends on a stable UI-visible order.
4. **`TelemetryService.statusHistories` — `referralStatus` + `fromStatus`**
   (surfaced by PR nine-eyes review, 2026-08-25) — both call sites use
   `referralEntity?.statusHistories?.firstOrNull()?…` for telemetry properties.
   `statusHistories` is a `MutableList` (not `MutableSet`) with
   `@OrderBy("createdAt DESC")`, so strictly out of the Set-audit scope, but
   shares the same tie-risk shape: if two status-history rows share the same
   `createdAt`, `.firstOrNull()` resolves via the DB's un-tiebroken result
   ordering. Recommend mirroring the `referralCohortHistories` fix in the
   same file — add an in-Kotlin
   `sortedWith(compareByDescending { createdAt }.thenBy { createdBy }.thenBy { referralStatusDescription.id })`
   (or an equivalent natural-attribute tiebreak) before `.firstOrNull()`.
   Cheap consistency fix; low functional risk today (status history rows are
   rarely created in the same millisecond) but worth doing for symmetry.
