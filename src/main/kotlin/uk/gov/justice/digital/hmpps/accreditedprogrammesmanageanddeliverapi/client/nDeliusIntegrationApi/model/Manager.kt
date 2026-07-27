package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class Manager(
  val staff: ProbationPractitioner,
  val team: CodedValue,
  val probationDeliveryUnit: CodedValue,
  val officeLocations: List<CodedValue>,
)
