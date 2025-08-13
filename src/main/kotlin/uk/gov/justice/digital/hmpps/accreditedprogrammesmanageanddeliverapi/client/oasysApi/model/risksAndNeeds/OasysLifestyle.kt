package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Lifestyle

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysLifestyle(
  val regActivitiesEncourageOffending: String?,
  val lifestyleIssuesDetails: String?,
  val easilyInfluenced: String?,
)

fun OasysLifestyle.toModel() = Lifestyle(
  activitiesEncourageOffending = regActivitiesEncourageOffending,
  lifestyleIssues = lifestyleIssuesDetails,
  easilyInfluenced = easilyInfluenced,
)
