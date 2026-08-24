# APG-2580 Follow-up 3 — SAR fixture drift guardrail (LOW PRIORITY)

**Parent ticket:** APG-2580 – TECH: Update SAR endpoint and report for Community following UAT
**Suggested branch name:** `APG-2580/sar-fixture-drift-guardrail`
**Base branch:** `main`
**Priority:** **LOW**
**Estimated effort:** half a day

---

## Motivation

Branch 5 (PR #877) uncovered a bug that was masked for months because the SAR HTML fixture `sar-expected-render-result.html` was hand-committable and the "No Data Held" output was accidentally the pinned baseline. Without a CI-enforced regen guardrail, the same class of bug can be masked again — any future silent-collapse or accidental-drop-of-data change becomes the new baseline the moment someone regenerates.

**Goal:** a CI job that regenerates both SAR fixtures and fails the build if the checked-in versions do not match. Regeneration becomes the only legal way to change a fixture; no more hand-edits.

---

## Design

### Verified facts (2026-08-24)

- Regenerator script exists at `scripts/local-scripts/regenerate-sar-snapshots.sh`. Implementer must open and read it first — behaviour, flags, and exit codes are pre-existing and must not be changed by this ticket.
- Fixtures live at:
  - `src/test/resources/sar/sar-api-response.json`
  - `src/test/resources/sar/sar-expected-render-result.html`
- There is currently **no CI gate** that runs the regenerator (verified — no such workflow in `.circleci/config.yml` or `.github/workflows/`; implementer must re-verify pre-flight).

### Suggested implementation (two options — pick one)

#### Option 1 (RECOMMENDED): a gradle task that fails on any post-regen diff

Add to `build.gradle.kts`:

```kotlin
tasks.register<Exec>("verifySarFixtures") {
  group = "verification"
  description = "Regenerates SAR fixtures and fails if they differ from the checked-in copies."
  workingDir = rootDir
  commandLine("scripts/local-scripts/regenerate-sar-snapshots.sh", "--check")
  // The script does not currently take a --check flag — this branch adds it.
}

tasks.named("check") {
  dependsOn("verifySarFixtures")
}
```

Then extend the script:

```bash
# scripts/local-scripts/regenerate-sar-snapshots.sh
# Add a --check mode that runs the existing regen into a temp dir and
# `diff -u` against the checked-in copies. Exit non-zero on any diff.
```

**Pros:** part of the standard `check` lifecycle → runs on every `./gradlew build` locally AND on CI without any CI config change.
**Cons:** slows every build by however long a regen takes (implementer to time it — likely ~30s).

#### Option 2: a dedicated CI job

Add a CircleCI job (verify actual CI provider — this repo currently uses CircleCI per `.circleci/config.yml`; implementer to re-confirm):

```yaml
- job:
    name: verify-sar-fixtures
    steps:
      - checkout
      - run: ./gradlew --no-daemon integrationTest -Dspring.profiles.active=integration-test
      - run: ./scripts/local-scripts/regenerate-sar-snapshots.sh
      - run: git diff --exit-code src/test/resources/sar/
```

**Pros:** doesn't slow local builds. Runs in parallel with the main test job.
**Cons:** requires CI config knowledge; doesn't help locally.

**Recommendation:** Option 1. Slower local builds are a fair price for eliminating a bug class that survived for months undetected.

---

## Pre-flight (MANDATORY)

```bash
# 1. Confirm regen script location and current behaviour
cat scripts/local-scripts/regenerate-sar-snapshots.sh

# 2. Confirm the two fixture paths
ls -la src/test/resources/sar/

# 3. Confirm no existing gate — expect zero hits
grep -rn "regenerate-sar-snapshots\|verifySarFixtures" .circleci/ .github/ build.gradle.kts 2>/dev/null

# 4. Confirm CI provider
ls -la .circleci/config.yml .github/workflows/ 2>/dev/null

# 5. Time a regen to know the local-build cost
time bash scripts/local-scripts/regenerate-sar-snapshots.sh
# Restore any drift after timing:
git checkout src/test/resources/sar/
```

---

## Verification checklist

- [ ] Pre-flight greps complete; counts recorded in PR body.
- [ ] Option chosen; rationale recorded in PR body.
- [ ] `verifySarFixtures` task (or CI job) runs the regen and diffs against checked-in fixtures.
- [ ] With **no code changes**, the gate PASSES (fixtures on `main` are already regen-stable — Branch 5 regenerated them).
- [ ] With a **deliberate hand-edit** to `sar-expected-render-result.html`, the gate FAILS with a clear message. Revert the edit. Record this negative test in the PR body.
- [ ] `./gradlew ktlintCheck` PASS.
- [ ] `./gradlew build` PASS (including the new gate).

---

## Anti-patterns

1. **Do not** make the gate auto-fix (auto-commit regen output). CI must be fail-loud, not silently rewrite files.
2. **Do not** change the existing regen script's default behaviour — only add a `--check` mode or a new script alongside.
3. **Do not** add this gate to a repo without also confirming CI has enough time budget (regen takes ~30s per pre-flight timing).

---

## Implementer prompt (paste into a fresh chat)

> **Ticket:** APG-2580 follow-up 3 — SAR fixture drift guardrail.
>
> **Base branch:** `main`. Confirm with `git log --oneline -3 main`.
>
> **Branch name:** `APG-2580/sar-fixture-drift-guardrail`.
>
> **Read these first:**
> 1. `docs/branches/APG-2580-followup-3-sar-fixture-drift-guardrail.md` (this doc)
> 2. `scripts/local-scripts/regenerate-sar-snapshots.sh` (the script you're extending)
>
> **Do the plan.** Recommend Option 1 (gradle task on `check`). Run every pre-flight command in the doc and paste output into the PR body.
>
> **Verification is non-negotiable:**
> - Positive test: gate PASSES on clean `main` + your changes.
> - Negative test: temporarily hand-edit `sar-expected-render-result.html`, confirm the gate FAILS with a clear diff message, revert the edit. Record before/after in the PR body.
> - `./gradlew build` — full green.
>
> **No guesswork rules:**
> - Do not change the existing regen script's default behaviour. Add a new mode or new script.
> - Do not add auto-commit / auto-fix behaviour. Fail loud.
> - Do not add the gate to non-SAR fixtures.
>
> **Report back with:**
> 1. Pre-flight output (script contents summary, timing, CI provider).
> 2. Chosen option and rationale.
> 3. Positive + negative test evidence.
> 4. PR URL + CI status.

