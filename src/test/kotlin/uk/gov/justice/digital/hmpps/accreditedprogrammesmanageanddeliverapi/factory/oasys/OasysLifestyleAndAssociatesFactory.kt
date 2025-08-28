package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn

class OasysLifestyleAndAssociatesFactory {
  private var regActivitiesEncourageOffending: String? = "1 - Some problems"
  private var lifestyleIssuesDetails: String? = "There are issues around involvement with drugs"
  private var crn: String? = randomCrn()

  fun withCrn(crn: String?) = apply { this.crn = crn }

  fun produce() = OasysLifestyleAndAssociates(
    regActivitiesEncourageOffending = this.regActivitiesEncourageOffending,
    lifestyleIssuesDetails = this.lifestyleIssuesDetails,
    crn = this.crn,
  )
}
