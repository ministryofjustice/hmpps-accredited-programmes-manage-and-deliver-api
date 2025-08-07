package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences

class OffencesFactory {
  private var mainOffence: Offence = OffenceFactory().produce()
  private var additionalOffences: List<Offence> = listOf(OffenceFactory().produce(), OffenceFactory().produce())

  fun withMainOffence(mainOffence: Offence) = apply { this.mainOffence = mainOffence }
  fun withAdditionalOffences(additionalOffences: List<Offence>) = apply { this.additionalOffences = additionalOffences }
  fun withNoAdditionalOffences() = apply { this.additionalOffences = emptyList() }

  fun produce() = Offences(
    mainOffence = this.mainOffence,
    additionalOffences = this.additionalOffences,
  )
}
