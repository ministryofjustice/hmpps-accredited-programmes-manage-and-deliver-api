package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class Offences(
  val mainOffence: Offence,
  val additionalOffences: List<Offence>,
)
