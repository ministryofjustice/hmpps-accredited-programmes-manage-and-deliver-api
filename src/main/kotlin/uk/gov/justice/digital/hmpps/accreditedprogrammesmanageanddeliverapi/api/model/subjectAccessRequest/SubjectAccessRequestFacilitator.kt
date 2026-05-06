package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity

data class SubjectAccessRequestFacilitator(
  val personName: String,
)

fun FacilitatorEntity.toApi() = SubjectAccessRequestFacilitator(
  personName = personName,
)
