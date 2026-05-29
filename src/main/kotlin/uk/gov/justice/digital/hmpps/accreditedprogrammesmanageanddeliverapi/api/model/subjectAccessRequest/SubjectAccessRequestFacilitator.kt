package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

data class SubjectAccessRequestFacilitator(
  val personName: String,
  val id: UUID?,
)

fun FacilitatorEntity.toApi() = SubjectAccessRequestFacilitator(
  personName = personName,
  id = id,
)
