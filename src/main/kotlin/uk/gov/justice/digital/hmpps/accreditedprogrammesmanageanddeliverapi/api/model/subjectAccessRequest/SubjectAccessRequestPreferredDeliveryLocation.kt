package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity

data class SubjectAccessRequestPreferredDeliveryLocation(
  val deliusCode: String,
  val deliusDescription: String,
  val probationDeliveryUnit: SubjectAccessRequestPreferredDeliveryLocationProbationDeliveryUnit,
)

fun PreferredDeliveryLocationEntity.toApi() = SubjectAccessRequestPreferredDeliveryLocation(
  deliusCode = deliusCode,
  deliusDescription = deliusDescription,
  probationDeliveryUnit = preferredDeliveryLocationProbationDeliveryUnit.toApi(),
)
