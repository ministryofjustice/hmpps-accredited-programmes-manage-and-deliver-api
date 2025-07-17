package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
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
}
