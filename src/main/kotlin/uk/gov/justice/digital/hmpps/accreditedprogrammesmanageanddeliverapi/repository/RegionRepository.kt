package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.RegionEntity

@Repository
interface RegionRepository : JpaRepository<RegionEntity, String> {
  @Query("SELECT r FROM RegionEntity r WHERE r.deletedAt IS NULL")
  fun findAllActive(): List<RegionEntity>
}
