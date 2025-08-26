package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PduEntity

@Repository
interface PduRepository : JpaRepository<PduEntity, Int> {

  /**
   * Find all active (non-deleted) PDUs.
   * 
   * @return List of active PDUs
   */
  @Query("SELECT p FROM PduEntity p WHERE p.deletedAt IS NULL")
  fun findAllActive(): List<PduEntity>
}
