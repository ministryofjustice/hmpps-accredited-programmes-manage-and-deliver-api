# APG-2580 Follow-up 2 — SAR `Set`-typed collection footgun guardrails (LOW PRIORITY)

**Parent ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Suggested branch name:** `APG-2580/sar-set-footgun-guardrails`
**Base branch:** `main` (after followup-1 merges — not strictly a dependency, but the two overlap on SAR DTO territory and it's cleaner to serialise them)
**Priority:** **LOW** — the bug this prevents is not currently present in the codebase (Branch 5 fixed the last occurrence). This is pure defensive hardening.
**Estimated effort:** 1 day for Part A alone; Part B is ~2 days on top.

---

## Background — the bug class this prevents

Branch 5 (PR #877) fixed a production-severity bug where three SAR sections silently rendered as `<p>No Data Held</p>` for every subject, despite the JSON containing real data.

**Root cause (verified — see `docs/branches/APG-2580-branch-5-render-test-seed-attendance-row.md`):**
- The SAR mustache template uses `{{#field.0}}…{{/field.0}}` as its "collection is non-empty" guard.
- JMustache resolves `.0` via indexed lookup (`get(0)`).
- Kotlin `MutableSet` does not expose indexed lookup, so `.0` on a Set → `null` → falsy → the inverted `{{^field.0}}<p>No Data Held</p>{{/field.0}}` fallback fires.
- The bug is silent — no compiler error, no runtime error, no failing test unless the fixture happens to exercise the specific field.

The bug shipped in PR #644 and was invisible until Branch 5's snapshot-coverage work exercised it. **Any future SAR DTO field added as `MutableSet` re-introduces the same silent-collapse bug.**

This follow-up puts guardrails in place so it cannot happen again.

---

## Two parts (implement A first; do B in a follow-up commit or a second PR)

### Part A — architecture test / lint rule (fast, cheap, high value)

Add a test that fails at CI time if any SAR DTO field is typed as `Set`/`MutableSet`.

**Suggested implementation:**

Create `src/test/kotlin/uk/gov/justice/digital/hmpps/accreditedprogrammesmanageanddeliverapi/api/model/subjectAccessRequest/SubjectAccessRequestDtoConventionsTest.kt`:

```kotlin
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class SubjectAccessRequestDtoConventionsTest {

  private val packageName =
    "uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest"

  @Test
  fun `no SAR DTO field may be typed as Set or MutableSet (mustache dot-zero idiom requires indexed collections)`() {
    val dtoClasses = findDataClassesIn(packageName)

    val violations = dtoClasses.flatMap { dto ->
      dto.memberProperties.mapNotNull { prop ->
        val returnType = prop.returnType.jvmErasure
        if (returnType == Set::class || returnType == MutableSet::class) {
          "${dto.simpleName}.${prop.name} : ${prop.returnType}"
        } else {
          null
        }
      }
    }

    assertThat(violations)
      .withFailMessage(
        """
        SAR DTO fields must not be typed as Set/MutableSet.
        The SAR mustache template uses {{#field.0}} (indexed lookup) as its
        non-empty guard, which returns null on a Set → sections silently render
        as "No Data Held". Use List/MutableList instead and sort deterministically
        in the mapper. See docs/branches/APG-2580-followup-2-sar-set-footgun-guardrails.md.

        Violations:
        """.trimIndent(),
      )
      .isEmpty()
  }

  private fun findDataClassesIn(pkg: String): List<KClass<*>> {
    // Implementer: verify which reflection library is already on the test classpath.
    // Options in this repo (verify via `./gradlew dependencies --configuration testRuntimeClasspath | grep -i reflect`):
    //  - org.reflections:reflections (if present) — use as shown above
    //  - Spring's ClassPathScanningCandidateComponentProvider (definitely on the classpath)
    //  - kotlin-reflect + a manual class-loader scan
    // Pick whichever is already a transitive dep; do not add a new one for this test.
    TODO("choose scanner based on existing classpath")
  }
}
```

**Pre-flight for Part A:**
- `./gradlew dependencies --configuration testRuntimeClasspath 2>&1 | grep -iE "reflections|classgraph"` — pick whichever is already present. If neither is, use Spring's `ClassPathScanningCandidateComponentProvider` with a `RegexPatternTypeFilter` for `.*` — it's guaranteed to be on the classpath.
- `grep -rn "package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest" src/main/kotlin | wc -l` — confirms how many DTO classes the test will scan. **Verified 2026-08-24: 25 classes in the package.**
- Run the new test **before** committing to confirm it passes on `main` (Branch 5 removed the last Set). If it fails, the rule caught a real violation — investigate.

**Deliverables:**
- One new test file.
- Green build.
- No production code changes.

### Part B — template helper `{{#hasAny …}}` (⚠️ COMPLEXITY WARNING — read before pursuing)

**Verified 2026-08-24, MUST READ BEFORE STARTING:** template rendering is NOT done in this repo. The SAR service in this repo (`SubjectAccessRequestService.kt`) implements `HmppsProbationSubjectAccessRequestService` from the external library `uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0` (see `build.gradle.kts:18`). The library owns:

- The template compiler / engine (mustache-family, not directly declared in this repo's `build.gradle.kts`).
- The rendering pipeline that produces the PDF/HTML output.
- Any helper / lambda registration API.

The `sar_template.mustache` at `src/main/resources/sar_template.mustache` is loaded and rendered *by the library*, not by any code in this repo. **This makes Part B significantly harder than originally documented.**

#### Option B1: upstream a `hasAny` helper into hmpps-kotlin-spring-boot-starter

Fork/PR into [hmpps-kotlin-lib](https://github.com/ministryofjustice/hmpps-kotlin-lib) (verify the exact repo — do not guess). Add the helper to whichever class configures the template compiler. Ship a new library version, bump `build.gradle.kts` in this repo, sweep the template.

**Pros:** benefits every SAR-emitting service across MoJ.
**Cons:** cross-repo change, requires library maintainer sign-off, weeks not days.

#### Option B2: local Spring bean override

Override the library's template-rendering bean in this repo with a custom implementation that pre-processes the template or adds the helper. Requires knowing the exact bean name and interface (verify by reading the library source or `HmppsProbationSubjectAccessRequestService` docs).

**Pros:** no cross-repo change.
**Cons:** fragile — every library version bump can break the override.

#### Option B3: do not do Part B

Given the complexity, **the strong recommendation is now to NOT do Part B in this ticket**. Part A alone (the architecture test) delivers the safety benefit without any of the risk. Part B only becomes interesting if a future SAR DTO field cannot be a `List` for some domain reason — which is not currently the case for any planned work.

If the user still wants Part B, ask them explicitly to choose B1 or B2 and confirm they understand the scope. **Do not start Part B without that explicit confirmation.**

**Pre-flight for Part B (if B1 or B2 chosen):**
- `grep -rn "hmpps-kotlin\|HmppsProbationSubjectAccessRequestService\|HmppsSubjectAccessRequestContent" src/main/kotlin build.gradle.kts` — confirms the library integration surface.
- Identify the library's template-rendering bean name (read `hmpps-kotlin-lib` source or its docs).
- `grep -c "\.0}}" src/main/resources/sar_template.mustache` — counts existing occurrences of the idiom to know the sweep size.
- Fixture regen required after the sweep.

**Trade-offs:**
- Part B makes the template unambiguous and future-proof, but **is a much larger change than originally thought** — see the complexity warning at the top of the Part B section. The rendering engine lives in `hmpps-kotlin-spring-boot-starter`, not this repo. Strong recommendation is Part A only.

---

## Anti-patterns (do NOT do these)

1. **Do not** change `ReferralEntity`'s `MutableSet` fields to `List`. JPA needs `Set` semantics for bag-fetch safety.
2. **Do not** add the guardrail to non-SAR DTOs. This is a template-specific concern; other JSON producers don't care.
3. **Do not** delete the `.0` idiom from the template in Part A. Part A only adds a test; Part B is what changes the template.
4. **Do not** add a new reflection dependency for Part A — reuse whatever is already on the classpath.

---

## Verification checklist

### Part A
- [ ] Reflection library selected from existing classpath (grep evidence in PR body).
- [ ] Test file created in the correct package.
- [ ] Test PASSES on current `main` (no violations expected).
- [ ] Test FAILS if temporarily hand-edited to add a `MutableSet` field to any SAR DTO (tested locally, not committed).
- [ ] `./gradlew ktlintCheck` PASS.
- [ ] `./gradlew build` PASS.

### Part B (if pursued)
- [ ] JMustache lambda API verified against the version in `build.gradle.kts`.
- [ ] Helper registered in whichever class compiles the template.
- [ ] All `.0}}` occurrences swept from `sar_template.mustache` (grep count → 0).
- [ ] Fixtures regenerated via `scripts/local-scripts/regenerate-sar-snapshots.sh`.
- [ ] HTML diff shows no rendered-output change (this is a refactor, not a fix).
- [ ] SAR tests all pass.

---

## Implementer prompt (paste into a fresh chat)

> **Ticket:** APG-2580 follow-up 2 — SAR Set-typed collection footgun guardrails.
>
> **Base branch:** `main`. Confirm via `git log --oneline -3 main`.
>
> **Branch name:** `APG-2580/sar-set-footgun-guardrails`.
>
> **Read these first:**
> 1. `docs/branches/APG-2580-followup-2-sar-set-footgun-guardrails.md` (this doc)
> 2. `docs/branches/APG-2580-branch-5-render-test-seed-attendance-row.md` (the bug this prevents)
> 3. `docs/branches/APG-2580-DELIVERY-TRACKER.md` correction #9 (root cause context)
>
> **Scope decision — do this first:** ask the user whether they want **Part A only** (default — fast, low risk, self-contained in this repo) or **Part A + Part B** (Part B is significantly larger than initially thought — see the plan doc's "COMPLEXITY WARNING" at the top of Part B; the rendering engine lives in the external `hmpps-kotlin-spring-boot-starter` library, so Part B involves either a cross-repo change (B1) or a fragile Spring bean override (B2)). **Recommend Part A only.** Do not implement Part B without explicit user opt-in AND a chosen sub-option (B1 or B2).
>
> **Do the plan.** Every code decision must be backed by grep evidence — do not guess reflection library, JMustache version, or template helper API. Paste grep counts into the PR body.
>
> **Verification is non-negotiable:**
> - `./gradlew ktlintCheck` — PASS.
> - `./gradlew test --tests "*SubjectAccessRequestDtoConventionsTest*"` — PASS on current main (no violations).
> - **Sanity check:** temporarily hand-edit any SAR DTO to add a `MutableSet` field, re-run the test, confirm it FAILS with a clear message. Revert the edit. Record this negative-test evidence in the PR body.
> - `./gradlew build` — full green.
>
> **No guesswork rules:**
> - Do not add new dependencies for reflection. Use what's already on the classpath.
> - Do not touch the mustache template unless the user opted into Part B.
> - Do not change any production DTO in Part A — this is test-only work.
>
> **Report back with:**
> 1. Reflection scanner choice + grep evidence.
> 2. Number of SAR DTO classes scanned.
> 3. Negative-test evidence (before/after screenshot or test output).
> 4. PR URL + CI status.
> 5. Any blockers.

