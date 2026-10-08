# [APG-XXXX](https://dsdmoj.atlassian.net/browse/APG-XXXX)

<!-- Replace the title above with the Jira ticket, or delete it for a `no-ticket/` branch. -->

## What does this change?

<!-- A short summary of the change and why it is needed. -->

## How has it been tested?

<!-- Unit/integration tests added, manual testing done, anything the reviewer should run locally. -->

---

## Data model changes

Does this PR add, remove or rename **any** of the following?

- [ ] A Flyway migration (`src/main/resources/db/migration/`)
- [ ] A JPA entity or a field on one (`.../entity/`)
- [ ] A field in the SAR response model (`.../api/model/subjectAccessRequest/`) or `SubjectAccessRequestService`
- [ ] The SAR report template (`src/main/resources/sar_template.mustache`)

**If you ticked any of the above, the SAR contract tests will fail until you update them.**

> [!IMPORTANT]
> We are legally obliged to return a complete and correct Subject Access Request for anyone whose data we
> hold. `SarContractIntegrationTest` is the guard for that — it fails on **any** data model change, deliberately,
> so that the question "should this new data appear in a SAR?" gets answered by a human rather than silently
> skipped.

### SAR checklist
- [ ] I have pre-approved any data model change with the sar team. A full guide can be found at https://dsdmoj.atlassian.net/wiki/spaces/NDSS/pages/6057492792/Subject+Access+Request+-+HMPPS+Digital
- [ ] I have decided whether the new/changed data **should be disclosed in a SAR**, and said so below
- [ ] If it should be disclosed, it is included in the SAR response model and rendered in `sar_template.mustache`
      using the correct [template helper](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api/blob/main/docs/how-to/update-sar-tests.md#template-helper-functions)
      (`formatDate`, `optionalValue`, `convertBoolean`, …)
- [ ] Snapshots regenerated with `./scripts/local-scripts/regenerate-sar-snapshots.sh`
      (**not** hand-edited) and the diff reviewed
- [ ] New Flyway migration → expected schema version bumped in **both** `SarContractIntegrationTest.kt` and
      `src/test/resources/application-test.yml`
- [ ] `./gradlew test --tests "*SarContractIntegrationTest*"` passes

Full guide: [How to update SAR tests](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api/blob/main/docs/how-to/update-sar-tests.md)

**SAR decision for this change:**

<!--
State the decision explicitly so the reviewer can check it, e.g.
- "Adds `attendee.withdrawal_reason` — personal data about the person on the programme, so it IS disclosed
   in the SAR report under the Attendance section."
- "Adds `session.created_by_job_id` — internal processing metadata, not personal data, so it is NOT disclosed.
   Snapshots regenerated because the entity schema changed."
-->

---

## Notes for Reviewer

<!-- Anything else: screenshots, feature flag name, follow-up tickets, deployment ordering, breaking API changes. -->
