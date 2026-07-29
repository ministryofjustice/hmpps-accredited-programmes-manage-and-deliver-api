package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class Manager(
  val staff: ProbationPractitioner,
  val team: CodeDescription,
  val probationDeliveryUnit: CodeDescription,
  val officeLocations: List<CodeDescription>,
)
