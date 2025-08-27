package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.AlcoholMisuseDetails
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAlcoholMisuseDetails(
  val currentUse: String?,
  val bingeDrinking: String?,
  val frequencyAndLevel: String?,
  val alcoholIssuesDetails: String?,
)

fun OasysAlcoholMisuseDetails.toModel(assessmentCompletedDate: LocalDate?) = AlcoholMisuseDetails(
  assessmentCompleted = assessmentCompletedDate,
  currentUse = currentUse,
  bingeDrinking = bingeDrinking,
  frequencyAndLevel = frequencyAndLevel,
  alcoholIssuesDetails = alcoholIssuesDetails,
)
