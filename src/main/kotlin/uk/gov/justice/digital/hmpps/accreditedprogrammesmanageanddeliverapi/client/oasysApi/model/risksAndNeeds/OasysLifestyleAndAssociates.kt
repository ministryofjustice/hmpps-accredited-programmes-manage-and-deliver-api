package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysLifestyleAndAssociates(
  val regActivitiesEncourageOffending: String?,
  val lifestyleIssuesDetails: String?,
  val crn: String?,
)

fun OasysLifestyleAndAssociates.toModel(assessmentCompletedDate: LocalDate?) = LifestyleAndAssociates(
  regActivitiesEncourageOffending = regActivitiesEncourageOffending,
  lifestyleIssuesDetails = lifestyleIssuesDetails,
  assessmentCompleted = assessmentCompletedDate,
)
