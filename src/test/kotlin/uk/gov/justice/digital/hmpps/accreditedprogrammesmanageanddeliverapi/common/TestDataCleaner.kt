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
      createNativeQuery("TRUNCATE TABLE availability CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE availability_slot CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE message_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_status_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_ldc_history CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE preferred_delivery_location_probation_delivery_unit CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE preferred_delivery_location CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE delivery_location_preferences CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_reporting_location CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE programme_group CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE programme_group_membership CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE programme_group_facilitator CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE programme_group_session_slot CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE facilitator CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE referral_motivation_background_and_non_associations CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE ndelius_appointment CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE session_attendance CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE attendee CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE session CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE data_import_record CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE im_data_import.iaps_licreqnos CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE im_data_import.reporting_location CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE im_data_import.referral CASCADE").executeUpdate()

      // Add additional tables here as the data model grows

      // Refresh our views after clearing tables
      createNativeQuery("REFRESH MATERIALIZED VIEW referral_caselist_item_view").executeUpdate()
      createNativeQuery("REFRESH MATERIALIZED VIEW group_waitlist_item_view").executeUpdate()
    }
  }
}
