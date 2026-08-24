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
- `grep -rn "package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest" src/main/kotlin | wc -l` — confirms how many DTO classes the test will scan (~15–20 expected).
- Run the new test **before** committing to confirm it passes on `main` (Branch 5 removed the last Set). If it fails, the rule caught a real violation — investigate.

**Deliverables:**
- One new test file.
- Green build.
- No production code changes.

### Part B — template helper `{{#hasAny …}}` (optional, larger scope)

Introduce a JMustache lambda / helper that expresses "collection has at least one element" without relying on the `.0` idiom, then sweep the template.

**Design sketch:**

Register a `hasAny` helper in the JMustache compiler (find the current registration site with `grep -rn "Mustache.compiler\|Mustache.Compiler\|Template" src/main/kotlin | grep -i sar` — likely in `SubjectAccessRequestService.kt` or a template config class).

```kotlin
private fun buildMustacheCompiler(): Mustache.Compiler =
  Mustache.compiler()
    .withLoader(...)
    // Existing helpers: formatDate, convertBoolean, optionalValue, getUserLastName, getIndexPlusOne, eq
    .withFormatter { obj ->
      // ...existing formatter...
    }
    // NEW: hasAny — usage: {{#hasAny collection}}…render…{{/hasAny}}{{^hasAny collection}}<p>No Data Held</p>{{/hasAny}}
    // Note: JMustache lambdas work via the object model, not compiler config — the actual
    // implementation is a Callable<String> field on the context, or a Mustache.Lambda impl.
    // Implementer must verify the JMustache version in build.gradle.kts and use the appropriate API.
```

Then sweep `src/main/resources/sar_template.mustache`:

```mustache
{{! BEFORE }}
{{#referralLdcHistories.0}}
  …table…
{{/referralLdcHistories.0}}
{{^referralLdcHistories.0}}
  <p>No Data Held</p>
{{/referralLdcHistories.0}}

{{! AFTER }}
{{#hasAny referralLdcHistories}}
  …table…
{{/hasAny}}
{{^hasAny referralLdcHistories}}
  <p>No Data Held</p>
{{/hasAny}}
```

**Pre-flight for Part B:**
- `grep -c "\.0}}" src/main/resources/sar_template.mustache` — counts existing occurrences of the idiom. Currently expected to be ~8–12 (Branch 5 didn't remove any — the template stayed the same, only the DTO types changed).
- Verify the JMustache version and lambda API: `grep -A 2 "com.samskivert\|jmustache" build.gradle.kts`
- Fixture regen required after the sweep.

**Trade-offs:**
- Part B makes the template unambiguous and future-proof, but it's a big diff for a template that's currently working. Only pursue if Part A's guardrail is felt to be insufficient (e.g. someone bypasses the test by adding a raw property on a domain class rather than a SAR DTO).

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
> **Scope decision — do this first:** ask the user whether they want **Part A only** (default — fast, low risk) or **Part A + Part B** (larger, template refactor). Do not implement Part B without explicit user opt-in. The plan doc lists trade-offs.
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

