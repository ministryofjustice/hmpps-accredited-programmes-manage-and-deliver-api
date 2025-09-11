package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import java.util.UUID

class PreferredDeliveryLocationEntityFactory(pduEntity: PreferredDeliveryLocationProbationDeliveryUnitEntity? = null) {
  private var id: UUID? = null
  private var deliusCode: String = randomAlphanumericString(6)
  private var deliusDescription: String = listOf(
    "London Probation Office",
    "Manchester Community Centre",
    "Birmingham Support Hub",
    "Leeds Supervision Office",
    "Bristol Community Office",
  ).random()
  private var preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnitEntity =
    pduEntity ?: PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()

  fun withId(id: UUID) = apply { this.id = id }
  fun withDeliusCode(deliusCode: String) = apply { this.deliusCode = deliusCode }
  fun withDeliusDescription(deliusDescription: String) = apply { this.deliusDescription = deliusDescription }
  fun withPreferredDeliveryLocationProbationDeliveryUnit(preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnitEntity) = apply { this.preferredDeliveryLocationProbationDeliveryUnit = preferredDeliveryLocationProbationDeliveryUnit }

  fun produce() = PreferredDeliveryLocationEntity(
    id = this.id,
    deliusCode = this.deliusCode,
    deliusDescription = this.deliusDescription,
    preferredDeliveryLocationProbationDeliveryUnit = this.preferredDeliveryLocationProbationDeliveryUnit,
  )
}
