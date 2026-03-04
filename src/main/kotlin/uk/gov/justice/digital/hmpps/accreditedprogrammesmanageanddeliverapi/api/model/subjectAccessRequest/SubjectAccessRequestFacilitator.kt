package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

data class SubjectAccessRequestFacilitator(
  val id: UUID?,
  val nDeliusPersonCode: String,
  val personName: String,
)

fun FacilitatorEntity.toApi() = SubjectAccessRequestFacilitator(
  id = id,
  nDeliusPersonCode = ndeliusPersonCode,
  personName = personName,
)
