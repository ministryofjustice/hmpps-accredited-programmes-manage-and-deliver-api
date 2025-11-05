package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

@Transactional
@Component
class TestDataGenerator {
  @PersistenceContext
  private lateinit var entityManager: EntityManager

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  fun createPreferredDeliveryLocationProbationDeliveryUnit(preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnitEntity) {
    entityManager.persist(preferredDeliveryLocationProbationDeliveryUnit)
  }

  fun createPreferredDeliveryLocation(preferredDeliveryLocation: PreferredDeliveryLocationEntity) {
    entityManager.persist(preferredDeliveryLocation)
  }

  fun createDeliveryLocationPreference(deliveryLocationPreferenceEntity: DeliveryLocationPreferenceEntity) {
    entityManager.persist(deliveryLocationPreferenceEntity)
  }

  fun createReferralWithDeliveryLocationPreferences(
    referralEntity: ReferralEntity,
    pdu: PreferredDeliveryLocationProbationDeliveryUnitEntity? = null,
    preferredDeliveryLocation: PreferredDeliveryLocationEntity? = null,
    deliveryLocationPreference: DeliveryLocationPreferenceEntity? = null,
  ) {
    createReferralWithStatusHistory(referralEntity)
    pdu?.let { createPreferredDeliveryLocationProbationDeliveryUnit(pdu) }
    preferredDeliveryLocation?.let { createPreferredDeliveryLocation(preferredDeliveryLocation) }
    deliveryLocationPreference?.let { createDeliveryLocationPreference(deliveryLocationPreference) }
  }

  fun createLdcHistoryForAReferral(referralLdcHistoryEntity: ReferralLdcHistoryEntity) {
    entityManager.persist(referralLdcHistoryEntity)
  }

  fun getReferralById(id: UUID): ReferralEntity = entityManager
    .createNativeQuery("SELECT * FROM referral r WHERE r.id = :referralId", ReferralEntity::class.java)
    .setParameter("referralId", id)
    .singleResult as ReferralEntity

  fun createAvailability(availabilityEntity: AvailabilityEntity) {
    entityManager.persist(availabilityEntity)
  }

  fun createReferralStatusDescriptionEntity(referralStatusDescriptionEntity: ReferralStatusDescriptionEntity) {
    entityManager.persist(referralStatusDescriptionEntity)
  }

  fun creatReferralStatusHistory(referralStatusHistoryEntity: ReferralStatusHistoryEntity) {
    entityManager.persist(referralStatusHistoryEntity)
  }

  fun createReferralWithStatusHistory(
    referralEntity: ReferralEntity? = null,
    referralStatusHistoryEntity: ReferralStatusHistoryEntity? = null,
  ) {
    val referral = referralEntity ?: ReferralEntityFactory().produce()

    val statusHistory = referralStatusHistoryEntity ?: ReferralStatusHistoryEntityFactory().produce(
      referral,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )

    entityManager.persist(referral)
    entityManager.persist(statusHistory)
  }

  fun <T> createReferralWithFields(
    referralEntity: ReferralEntity? = null,
    fields: List<T>,
  ) {
    entityManager.persist(referralEntity)
    for (field in fields) {
      entityManager.persist(field)
    }
  }

  fun createReferralWithStatusHistory(
    referralEntity: ReferralEntity? = null,
    statusDescriptionList: List<ReferralStatusDescriptionEntity>,
  ) {
    val referral = referralEntity ?: ReferralEntityFactory().produce()
    entityManager.persist(referral)

    statusDescriptionList.forEach {
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, it)
      entityManager.persist(statusHistory)
    }
  }

  fun createGroupMembership(
    programmeGroupMembership: ProgrammeGroupMembershipEntity,
  ) {
    entityManager.persist(programmeGroupMembership)
  }

  fun createReferralWithReportingLocation(referralReportingLocationEntity: ReferralReportingLocationEntity) {
    entityManager.persist(referralReportingLocationEntity)
  }

  fun createReferralWithReportingLocationAndStatusHistory(
    referralEntity: ReferralEntity,
    referralStatusHistoryEntity: ReferralStatusHistoryEntity,
    referralReportingLocationEntity: ReferralReportingLocationEntity,
  ) {
    entityManager.persist(referralEntity)
    entityManager.persist(referralStatusHistoryEntity)
    entityManager.persist(referralReportingLocationEntity)
  }

  fun createReferralStatusHistory(
    referralStatusHistoryEntity: ReferralStatusHistoryEntity,
  ) {
    entityManager.persist(referralStatusHistoryEntity)
  }

  fun refreshReferralCaseListItemView() {
    entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW referral_caselist_item_view").executeUpdate()
  }

  fun createGroup(
    group: ProgrammeGroupEntity,
  ) {
    entityManager.persist(group)
  }
}
