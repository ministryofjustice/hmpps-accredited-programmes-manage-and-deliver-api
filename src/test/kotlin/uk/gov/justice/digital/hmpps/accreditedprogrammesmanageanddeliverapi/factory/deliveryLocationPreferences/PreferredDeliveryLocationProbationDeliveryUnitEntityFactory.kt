package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import java.util.UUID

class PreferredDeliveryLocationProbationDeliveryUnitEntityFactory {
  private var id: UUID? = null
  private var deliusCode: String = randomAlphanumericString(8)
  private var deliusDescription: String = listOf(
    "North West Probation Delivery Unit",
    "South East Probation Delivery Unit",
    "Midlands Probation Delivery Unit",
    "Yorkshire Probation Delivery Unit",
    "London Probation Delivery Unit",
  ).random()

  fun withId(id: UUID) = apply { this.id = id }
  fun withDeliusCode(deliusCode: String) = apply { this.deliusCode = deliusCode }
  fun withDeliusDescription(deliusDescription: String) = apply { this.deliusDescription = deliusDescription }

  fun produce() = PreferredDeliveryLocationProbationDeliveryUnitEntity(
    id = this.id,
    deliusCode = this.deliusCode,
    deliusDescription = this.deliusDescription,
  )
}
