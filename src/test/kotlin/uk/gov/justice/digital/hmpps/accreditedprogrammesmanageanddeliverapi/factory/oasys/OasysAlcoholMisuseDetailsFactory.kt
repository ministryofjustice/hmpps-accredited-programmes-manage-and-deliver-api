package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAlcoholMisuseDetails

class OasysAlcoholMisuseDetailsFactory {
  private var currentUse: String? = "1-Some problems"
  private var bingeDrinking: String? = "1-Some problems"
  private var frequencyAndLevel: String? = "2-Significant problems"
  private var alcoholIssuesDetails: String? = "Alcohol dependency affecting employment and relationships"

  fun withCurrentUse(currentUse: String?) = apply {
    this.currentUse = currentUse
  }

  fun withBingeDrinking(bingeDrinking: String?) = apply {
    this.bingeDrinking = bingeDrinking
  }

  fun withFrequencyAndLevel(frequencyAndLevel: String?) = apply {
    this.frequencyAndLevel = frequencyAndLevel
  }

  fun withAlcoholIssuesDetails(alcoholIssuesDetails: String?) = apply {
    this.alcoholIssuesDetails = alcoholIssuesDetails
  }

  fun produce() = OasysAlcoholMisuseDetails(
    currentUse = this.currentUse,
    bingeDrinking = this.bingeDrinking,
    frequencyAndLevel = this.frequencyAndLevel,
    alcoholIssuesDetails = this.alcoholIssuesDetails,
  )
}
