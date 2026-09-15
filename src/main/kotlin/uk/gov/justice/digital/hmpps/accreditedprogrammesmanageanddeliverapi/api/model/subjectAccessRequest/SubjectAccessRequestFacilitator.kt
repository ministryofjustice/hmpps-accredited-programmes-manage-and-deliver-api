package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.toSurname

data class SubjectAccessRequestFacilitator(
  val personName: String,
)

/**
 * Default projection — emits the facilitator's stored full name.
 *
 * Used by the Facilitators-roster section of the SAR output
 * (`sessionFacilitators[].facilitator`). Full name is intentional here — the
 * roster reads more naturally with "Joe Bloggs" than "Bloggs". See
 * `docs/APG-2580-sar-recorded-by-surname-only.md` §2.1.
 */
fun FacilitatorEntity.toApi() = SubjectAccessRequestFacilitator(
  personName = personName,
)

/**
 * SAR-specific projection that emits surname only (particles preserved,
 * e.g. `van der Berg`).
 *
 * Used by the "Recorded by" field of the SAR output
 * (`attendances[].recordedByFacilitator`). Introduced in APG-2580 to comply
 * with the round-2 QAT data-dictionary rule "surname only". Falls back to
 * the full name if [toSurname] returns null so the non-nullable `personName`
 * field is never populated with a blank string. See
 * `docs/APG-2580-sar-recorded-by-surname-only.md` §4.3.
 */
fun FacilitatorEntity.toSarRecordedByApi(): SubjectAccessRequestFacilitator = SubjectAccessRequestFacilitator(personName = toSurname(personName) ?: personName)
