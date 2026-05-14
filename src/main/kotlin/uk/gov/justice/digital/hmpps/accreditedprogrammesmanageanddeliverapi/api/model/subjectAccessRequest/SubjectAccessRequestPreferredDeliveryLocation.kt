package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity

data class SubjectAccessRequestPreferredDeliveryLocation(
  val deliusDescription: String,
  val probationDeliveryUnit: SubjectAccessRequestPreferredDeliveryLocationProbationDeliveryUnit,
)

fun PreferredDeliveryLocationEntity.toApi() = SubjectAccessRequestPreferredDeliveryLocation(
  deliusDescription = deliusDescription,
  probationDeliveryUnit = preferredDeliveryLocationProbationDeliveryUnit.toApi(),
)
