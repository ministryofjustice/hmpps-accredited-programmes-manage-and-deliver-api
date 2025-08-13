package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAlcoholDetail(
  val alcoholLinkedToHarm: String?,
  val alcoholIssuesDetails: String?,
  val frequencyAndLevel: String?,
  val bingeDrinking: String?,
)
