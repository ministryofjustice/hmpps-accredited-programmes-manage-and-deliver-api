package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity

@Transactional
@Component
class TestDataGenerator {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  fun createReferral(referralEntity: ReferralEntity) {
    entityManager.persist(referralEntity)
  }

  fun createPreferredDeliveryLocationProbationDeliveryUnit(preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnit) {
    entityManager.persist(preferredDeliveryLocationProbationDeliveryUnit)
  }

  fun createPreferredDeliveryLocation(preferredDeliveryLocation: PreferredDeliveryLocation) {
    entityManager.persist(preferredDeliveryLocation)
  }

  fun createDeliveryLocationPreference(deliveryLocationPreferenceEntity: DeliveryLocationPreferenceEntity) {
    entityManager.persist(deliveryLocationPreferenceEntity)
  }

  fun createAvailability(availabilityEntity: AvailabilityEntity) {
    entityManager.persist(availabilityEntity)
  }

  fun creatReferralStatusHistory(referralStatusHistoryEntity: ReferralStatusHistoryEntity) {
    entityManager.persist(referralStatusHistoryEntity)
  }

  fun createReferralWithStatusHistory(
    referralEntity: ReferralEntity,
    referralStatusHistoryEntity: ReferralStatusHistoryEntity,
  ) {
    entityManager.persist(referralEntity)
    entityManager.persist(referralStatusHistoryEntity)
    referralEntity.statusHistories.add(referralStatusHistoryEntity)
    entityManager.merge(referralEntity)
  }

  fun refreshReferralCaseListItemView() {
    entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW referral_caselist_item_view").executeUpdate()
  }
}
