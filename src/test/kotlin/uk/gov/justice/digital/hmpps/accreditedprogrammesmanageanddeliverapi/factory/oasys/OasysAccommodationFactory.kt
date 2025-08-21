package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAccommodation

class OasysAccommodationFactory {
  private var noFixedAbodeOrTransient: String? = "Yes"

  fun withNoFixedAbodeOrTransient(noFixedAbodeOrTransient: String?) = apply { this.noFixedAbodeOrTransient = noFixedAbodeOrTransient }

  fun produce() = OasysAccommodation(
    noFixedAbodeOrTransient = this.noFixedAbodeOrTransient,
  )
}
