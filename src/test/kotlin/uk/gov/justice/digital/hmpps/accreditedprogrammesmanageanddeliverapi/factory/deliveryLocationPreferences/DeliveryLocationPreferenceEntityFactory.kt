package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class DeliveryLocationPreferenceEntityFactory(
  referral: ReferralEntity? = null,
  pduEntity: PreferredDeliveryLocationProbationDeliveryUnitEntity? = null,
) {

  private val entity = pduEntity ?: PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()
  private var id: UUID? = null
  private var referral: ReferralEntity = referral ?: ReferralEntityFactory().produce()
  private var createdBy: String = "TEST_USER"
  private var createdAt: LocalDateTime = LocalDateTime.now().minusDays(Random.nextLong(1, 30))
  private var lastUpdatedAt: LocalDateTime = LocalDateTime.now().minusHours(Random.nextLong(1, 24))
  private var locationsCannotAttendText: String? = randomSentence(wordRange = 5..15)
  private var preferredDeliveryLocations: MutableSet<PreferredDeliveryLocationEntity> = mutableSetOf(
    PreferredDeliveryLocationEntityFactory().withPreferredDeliveryLocationProbationDeliveryUnit(entity).produce(),
    PreferredDeliveryLocationEntityFactory().withPreferredDeliveryLocationProbationDeliveryUnit(entity).produce(),
  )

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withLastUpdatedAt(lastUpdatedAt: LocalDateTime) = apply { this.lastUpdatedAt = lastUpdatedAt }
  fun withLocationsCannotAttendText(locationsCannotAttendText: String?) = apply { this.locationsCannotAttendText = locationsCannotAttendText }

  fun withPreferredDeliveryLocations(preferredDeliveryLocations: MutableSet<PreferredDeliveryLocationEntity>) = apply { this.preferredDeliveryLocations = preferredDeliveryLocations }

  fun produce() = DeliveryLocationPreferenceEntity(
    id = this.id,
    referral = this.referral,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    lastUpdatedAt = this.lastUpdatedAt,
    locationsCannotAttendText = this.locationsCannotAttendText,
    preferredDeliveryLocations = this.preferredDeliveryLocations,
  )
}
