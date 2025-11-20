package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusApiProbationDeliveryUnitWithOfficeLocations(
  val code: String,
  val description: String,
  val officeLocations: List<CodeDescription>,
)
