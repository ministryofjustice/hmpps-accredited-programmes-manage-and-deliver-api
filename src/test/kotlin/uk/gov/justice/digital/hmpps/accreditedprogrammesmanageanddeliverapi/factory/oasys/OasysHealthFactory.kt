package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysHealth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn

class OasysHealthFactory {
  private var generalHealth: String? = listOf("Yes", "No", null).random()
  private var generalHeathSpecify: String? = "Has chronic conditions"
  private var crn: String? = randomCrn()

  fun withGeneralHealth(generalHealth: String?) = apply {
    this.generalHealth = generalHealth
  }

  fun withGeneralHeathSpecify(generalHeathSpecify: String?) = apply { this.generalHeathSpecify = generalHeathSpecify }

  fun withCrn(crn: String?) = apply { this.crn = crn }

  fun produce() = OasysHealth(
    generalHealth = this.generalHealth,
    generalHeathSpecify = this.generalHeathSpecify,
    crn = this.crn,
  )
}
