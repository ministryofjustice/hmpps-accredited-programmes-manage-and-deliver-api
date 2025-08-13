package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class NDeliusRegistrations(
  val registrations: List<NDeliusRegistration>,
)

data class NDeliusRegistration(
  @get:JsonProperty("type", required = true)
  val type: CodeDescription,
  @get:JsonProperty("category", required = false)
  val category: CodeDescription? = null,
  @get:JsonProperty("date", required = true)
  val date: String,
  @get:JsonProperty("nextReviewDate", required = false)
  val nextReviewDate: String? = null,
)

fun NDeliusRegistrations.listOfActiveRegistrations() = this.registrations.map { nDeliusRegistration -> nDeliusRegistration.type.description }
