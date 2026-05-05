package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import java.util.UUID

data class SubjectAccessRequestPreferredDeliveryLocationProbationDeliveryUnit(
  val deliusCode: String,
  val deliusDescription: String,
  val id: UUID?,
)

fun PreferredDeliveryLocationProbationDeliveryUnitEntity.toApi() = SubjectAccessRequestPreferredDeliveryLocationProbationDeliveryUnit(
  deliusCode = deliusCode,
  deliusDescription = deliusDescription,
  id = id,
)
