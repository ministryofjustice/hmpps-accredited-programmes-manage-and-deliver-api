# APG-2580 Follow-up 4 — Entity-side `MutableSet` iteration-order audit (LOW PRIORITY)

**Parent ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Suggested branch name:** `APG-2580/entity-set-iteration-audit`
**Base branch:** `main`
**Priority:** **LOW**
**Estimated effort:** half a day (audit is the whole ticket — code changes only if the audit finds real risk)

---

## Motivation

Correction #8 (tracker) and the Branch 5 root-cause investigation both surfaced that `MutableSet` on the *entity* side is JPA-correct but has no guaranteed iteration order. Hibernate uses `LinkedHashSet` by default, so iteration is **insertion-order** in practice — but this is:

- Not part of any documented contract.
- Sensitive to Hibernate version bumps (Correction #8 was caused by a Hibernate/byte-buddy bump).
- Sensitive to eager-vs-lazy load and join-fetch strategies.
- Not enforced by any test.

Any code that iterates one of these Sets and treats the *first*, *last*, or *ordinal* element as meaningful is a latent flake.

**Goal:** a one-time audit of all `MutableSet<X>` fields on `@Entity` classes, checking every call-site for order-sensitive usage. Fix findings; document tolerances; add follow-up tickets if bigger changes are needed.

---

## Scope

The audit covers `MutableSet` fields on `@Entity`-annotated classes in `src/main/kotlin/.../entity/`. `Set` (immutable) is out of scope — Hibernate doesn't use it.

**Confirmed grep target (2026-08-24):**

```bash
grep -rn ": MutableSet<" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/
```

Implementer must re-run this and enumerate every hit.

---

## Audit process

For every `MutableSet<X>` field found:

### Step 1: catalogue

For each field, record:
- Owning entity + field name.
- Whether it has an `@OrderBy` annotation (if yes, iteration order is DB-enforced and low-risk).
- Whether the entity uses `@OrderColumn` (fully explicit ordering, very low risk).

### Step 2: find call-sites

```bash
grep -rn "\.<fieldName>\b" src/main/ src/test/ --include="*.kt"
```

For each call-site, classify as:

- **Safe:** `.size`, `.isEmpty()`, `.contains(x)`, `.any { ... }`, `.forEach { ... }` where side-effects don't depend on order, `.toSet()`.
- **Order-sensitive (needs review):** `.first()`, `.last()`, `.firstOrNull()`, `.lastOrNull()`, `.take(n)`, `.drop(n)`, `.elementAt(n)`, `.indexOfFirst { ... }`, any code that reads the collection as a list or serialises it.
- **Silently order-sensitive:** `.maxByOrNull { ... }` / `.minByOrNull { ... }` with a comparator that has ties (deterministic on `{selector}` alone if the selector has a total order; flaky otherwise).

### Step 3: verify each order-sensitive site

For every hit in category 2 or 3:

- Is there an `@OrderBy` on the field? If not, is the intent to depend on insertion order?
- Is there a natural total ordering (createdAt + id, or similar) that makes the code deterministic?
- If neither: **finding recorded**.

### Step 4: fix findings

For each finding:

- **Preferred:** add an `@OrderBy` to the field (JPA-level fix, benefits every consumer at once).
- **Or:** replace the call-site with an explicit `.sortedWith(...)` before `.first()`/`.last()` etc.
- **Or:** if the call-site tolerates any element (e.g. tests that assert non-empty then read a value that doesn't vary), add a comment explaining the assumption.

---

## Known suspects (as of 2026-08-24 — implementer must re-verify)

Based on the Branch 5 grep sweep, these entity Set fields are known:

| Entity | Field | Known consumers | Notes |
|---|---|---|---|
| `ReferralEntity` | `programmeGroupMemberships` | `ReferralStatusService.maxByOrNull { it.createdAt }` (x2), `ProgrammeGroupMembershipService.add(...)`, DTO mapper (now sorted) | `maxByOrNull` on `createdAt` alone → tie-break flake if two memberships share `createdAt`. Consider `.thenBy { it.id }` or add `@OrderBy("createdAt ASC")` |
| `ReferralEntity` | `referralLdcHistories` | `ReferralControllerIntegrationTest.maxByOrNull { it.createdAt!! }`, DTO mapper (now sorted) | Same tie-break concern as above |
| `ReferralEntity` | `referralCohortHistories` | `Referral.kt` model `maxByOrNull { it.createdAt }`, **`TelemetryService.kt:39` `.firstOrNull()`** (verified 2026-08-24), tests use `.first()` and `.maxByOrNull`, DTO mapper (now sorted DESC) | **`TelemetryService.firstOrNull()` reads insertion-order (LinkedHashSet) and treats that first row's cohort as the telemetry value — verified against source. If the intent is "oldest known cohort" this is intent-correct but fragile; if the intent is "most recent cohort" it's actively wrong. This is a confirmed at-risk finding — must be inspected and either fixed or annotated during the audit.** |
| `ProgrammeGroupEntity` | `programmeGroupMemberships` | `GroupDetailsResponse.count { ... }` (order-agnostic), tests use `.first { predicate }` (order-agnostic given the predicate) | Safe. |

**Implementer:** treat this table as a starting sketch, not gospel. Re-grep and re-verify every row before adjusting anything.

---

## Findings-based decision tree

- **0 findings:** commit the audit report as a Markdown doc under `docs/`, close the ticket.
- **1–3 low-risk findings:** fix in-place on this branch.
- **4+ findings, or any high-risk finding:** commit the audit report; open separate tickets per fix; close this ticket after the report is merged.

---

## Pre-flight (MANDATORY)

```bash
# 1. Enumerate the fields
grep -rn ": MutableSet<" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/ | wc -l

# 2. Enumerate @OrderBy usage
grep -rn "@OrderBy" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/entity/

# 3. Enumerate order-sensitive call patterns
grep -rn "\.first()\|\.last()\|\.firstOrNull()\|\.lastOrNull()\|\.maxByOrNull\|\.minByOrNull" src/main/kotlin --include="*.kt" | wc -l

# 4. Confirm the Branch 5 fixes are still in place
grep -A 3 "referralCohortHistories =\|referralLdcHistories =\|programmeGroupMemberships =" src/main/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestReferral.kt
```

Paste all outputs into the PR body under a "Pre-flight" section.

---

## Deliverable

A Markdown report at `docs/APG-2580-entity-set-iteration-audit.md`:

```markdown
# APG-2580 Entity `MutableSet` iteration-order audit — <date>

## Scope
<paste the grep count from pre-flight step 1>

## Fields catalogued
<one section per field>

### <Entity>.<field>
- Type: `MutableSet<X>`
- `@OrderBy`: <annotation or "none">
- Consumers:
  - `<file:line>` — <call pattern> — <classification: safe / order-sensitive / silently order-sensitive>
- Verdict: <safe / at-risk / fixed on this branch>
- Fix (if any): <link to commit>
```

Plus:
- Any code fixes (if verdict was "at-risk" and low-risk enough to include here).
- Any new follow-up tickets referenced by number.

---

## Anti-patterns

1. **Do not** change entity Set fields to List. JPA needs Set semantics for many-to-many and one-to-many bag safety.
2. **Do not** add `@OrderBy` to a field without checking whether it introduces a query cost (usually negligible, but confirm for high-traffic paths).
3. **Do not** batch large refactors into this branch. The audit is the deliverable; heavy refactors go to their own tickets.

---

## Implementer prompt (paste into a fresh chat)

> **Ticket:** APG-2580 follow-up 4 — Entity-side `MutableSet` iteration-order audit.
>
> **Base branch:** `main`. Confirm with `git log --oneline -3 main`.
>
> **Branch name:** `APG-2580/entity-set-iteration-audit`.
>
> **Read these first:**
> 1. `docs/branches/APG-2580-followup-4-entity-set-iteration-audit.md` (this doc)
> 2. `docs/branches/APG-2580-DELIVERY-TRACKER.md` correction #8 (the Hibernate-version-dependent flake that motivated this audit)
>
> **This is primarily research, not code changes.** Follow the audit process (Steps 1–4) exactly. Do not skip cataloguing fields you think are "obviously safe" — the whole point is a systematic sweep.
>
> **Pre-flight is mandatory.** Every grep in the doc must be run and every count recorded in the PR body.
>
> **Decision rules (from the doc):**
> - 0 findings → commit the report only.
> - 1–3 low-risk findings → fix on this branch + commit the report.
> - 4+ findings or any high-risk finding → commit the report only, open separate tickets for each fix.
>
> **No guesswork rules:**
> - Every field verdict must be backed by grep evidence. No "looks safe to me" without call-site enumeration.
> - Do not change entity Set → List. Ever.
> - Do not add `@OrderBy` speculatively — only where the audit shows a real risk.
>
> **Verification:**
> - `./gradlew build` PASS after any code changes.
> - Report file exists, is readable, and every catalogued field has a verdict.
>
> **Report back with:**
> 1. Grep counts from pre-flight.
> 2. Number of fields catalogued.
> 3. Number of findings by category (safe / at-risk / fixed).
> 4. Links to any follow-up tickets opened.
> 5. PR URL + CI status.

