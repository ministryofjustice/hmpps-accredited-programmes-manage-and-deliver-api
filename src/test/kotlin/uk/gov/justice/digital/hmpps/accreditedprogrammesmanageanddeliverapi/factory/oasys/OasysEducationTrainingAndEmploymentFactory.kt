package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysEducationTrainingAndEmployment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn

class OasysEducationTrainingAndEmploymentFactory {
  private var learningDifficulties: String? = "0-No problems"
  private var crn: String? = randomCrn()

  fun withCrn(crn: String?) = apply { this.crn = crn }

  fun produce() = OasysEducationTrainingAndEmployment(
    learningDifficulties = this.learningDifficulties,
    crn = this.crn,
  )
}
