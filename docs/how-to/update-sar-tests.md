# How to Update SAR Tests

This guide explains how to update the Subject Access Request (SAR) tests when changes are made to the SAR endpoint, data model, or report template.

## Overview

The SAR tests are located in:

```
src/test/kotlin/.../sar/SarContractIntegrationTest.kt
src/test/kotlin/.../sar/HmppsSubjectAccessRequestControllerIntegrationTest.kt
```

There are four contract test interfaces that `SarContractIntegrationTest` implements:

| Interface | Purpose |
|---|---|
| `SarApiDataTest` | Validates the JSON shape of the SAR API response |
| `SarReportTest` | Validates the rendered HTML report against a snapshot |
| `SarFlywaySchemaTest` | Validates the current Flyway schema version |
| `SarJpaEntitiesTest` | Validates the JPA entity schema snapshot |

Each has a corresponding snapshot/fixture file under `src/test/resources/sar/`.

---

## Snapshot Files

| File | Purpose |
|---|---|
| `src/test/resources/sar/sar-api-response.json` | Expected JSON response from the SAR API |
| `src/test/resources/sar/sar-api-expected-render-result.html` | Expected rendered HTML report |
| `src/test/resources/sar/entity-schema-snapshot.json` | Expected JPA entity schema |

> **Note:** Snapshot files store normalised content — UUIDs are replaced with `<UUID>` and all datetime values with `<DATE_TIME>` — so they never need updating just because timestamps or IDs change between runs.

---

## Regenerating Snapshots

Whenever a change requires the snapshots to be updated, use the provided script rather than updating files manually:

```bash
chmod +x scripts/local-scripts/regenerate-sar-snapshots.sh  # only needed once
./scripts/local-scripts/regenerate-sar-snapshots.sh
```

This script:
1. Runs all three snapshot tests in a single Gradle invocation with `SAR_GENERATE_ACTUAL=true`
2. Copies the generated `.log` files into `src/test/resources/sar/` with the correct snapshot filenames
3. Deletes the temporary `.log` files

Once complete, verify everything passes:

```bash
./gradlew test --tests "*SarContractIntegrationTest*"
```

---

## Scenario 1: Changing the SAR API Response (Data Model)

If you add, remove, or rename fields in the SAR API response (e.g. changes to entities included in the SAR data):

1. Run the tests to see the failure:
   ```bash
   ./gradlew test --tests "*SarContractIntegrationTest*"
   ```

2. The `SAR API should return expected data` test will fail showing the diff between the actual and expected JSON.

3. Re-generate the snapshots using the script above.

---

## Scenario 2: Changing the Report Template (`sar_template.mustache`)

If you modify `src/main/resources/sar_template.mustache`:

1. Run the tests:
   ```bash
   ./gradlew test --tests "*SarContractIntegrationTest*"
   ```

2. The `SAR report should render as expected` test will fail with a diff of the normalised HTML.

3. Re-generate the snapshots using the script above.

### Tips for reviewing the snapshot

- Dates that are not datetimes (e.g. `13 April 1994`, `12 May 2028`) are **not** normalised and must appear verbatim in the snapshot.
- Watch out for **special characters** — session names containing curly apostrophes (e.g. `Managing life's problems`) must use the exact character from the data, not a straight apostrophe (`'`).
- The `<style>` block at the top of the snapshot is injected by the SAR rendering service and should not be changed unless the upstream styles change.

---

## Scenario 3: Adding New Test Data in `setupTestData()`

If the test data is extended (e.g. adding a new referral, group, or attendance record):

1. Update `setupTestData()` in `SarContractIntegrationTest.kt`.
2. Re-generate the snapshots using the script above.

---

## Scenario 4: Adding a New Entity to the SAR JPA Schema

If a new JPA entity is added that should be tracked in the SAR entity schema:

1. Run the tests — the `JPA generated entity schema should match expected snapshot` test will fail.
2. Re-generate the snapshots using the script above.

---

## Scenario 5: Flyway Schema Version Changes

After adding a new Flyway migration, update the expected version in two places:

1. `SarContractIntegrationTest.kt`:
   ```kotlin
   private val sarIntegrationTestHelper by lazy {
     SarIntegrationTestHelper(
       ...
       expectedFlywaySchemaVersion = "102", // increment this
       ...
     )
   }
   ```

2. `src/test/resources/application-test.yml`:
   ```yaml
   hmpps:
     sar:
       tests:
         expected-flyway-schema-version: 102
   ```

---

## Dynamic Value Normalisation

The tests apply `normalizeDynamicValues` to both the actual output and the snapshot before comparing, so snapshots are never sensitive to exact timestamps or UUIDs. The following are replaced automatically:

| Regex | Replaced with | Matches |
|---|---|---|
| `[0-9a-fA-F]{8}-...-[0-9a-fA-F]{12}` | `<UUID>` | UUID values |
| `\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?` | `<DATE_TIME>` | ISO datetime strings |
| `\d{1,2} \w+ \d{4}, \d{1,2}:\d{2}:\d{2} (?:am\|pm)` | `<DATE_TIME>` | Human-readable datetime strings (output of `formatDate` helper) |

Because normalisation is applied before saving via the script, the snapshot files themselves contain `<DATE_TIME>` and `<UUID>` placeholders.

If the SAR template starts using a new date/time format that does not match any of these patterns, add a new regex to `normalizeDynamicValues` in `SarContractIntegrationTest.kt`.

---

## Template Helper Functions

The report template (`sar_template.mustache`) uses the following helper functions provided by the SAR integration framework. When adding new fields to the template, use the appropriate helper:

| Helper | Use for |
|---|---|
| `formatDate` | All date and datetime fields |
| `optionalValue` | All string/text fields (returns `No Data Held` if null/empty) |
| `convertBoolean` | Boolean fields (renders `Yes` / `No`) |
| `getUserLastName` | Username fields — transforms username to staff surname |
| `getPrisonName` | Prison/caseload ID fields |
| `getLocationNameByDpsId` | DPS location ID fields |
| `getLocationNameByNomisId` | NOMIS location ID fields |
| `convertCamelCase` | Camel case enum/code values |

See the [SAR Integration Guide](https://dsdmoj.atlassian.net/wiki/spaces/NDSS/pages/4780589084/Integration+Guide#Template-Helper-Functions) for full documentation.
