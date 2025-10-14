package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Transactional
@Component
class TestDataCleaner(
  @Autowired
  private val entityManager: EntityManager,
) {
  fun cleanAllTables() {
    entityManager.apply {
      createNativeQuery("TRUNCATE TABLE message_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_status_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_ldc_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE availability CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE availability_slot CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE preferred_delivery_location_probation_delivery_unit CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE preferred_delivery_location CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE delivery_location_preferences CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_reporting_location CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE groups CASCADE").executeUpdate()

      // Add additional tables here as the data model grows
    }
  }
}
