package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class NDeliusApiProbationDeliveryUnitWithOfficeLocations(
  val code: String,
  val description: String,
  val officeLocations: List<NDeliusApiOfficeLocation>,
)
