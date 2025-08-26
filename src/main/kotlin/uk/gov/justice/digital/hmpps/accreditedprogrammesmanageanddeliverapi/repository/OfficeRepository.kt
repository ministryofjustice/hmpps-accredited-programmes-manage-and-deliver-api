package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.OfficeEntity

@Repository
interface OfficeRepository : JpaRepository<OfficeEntity, String> {

  @Query("SELECT o FROM OfficeEntity o WHERE o.deletedAt IS NULL")
  fun findAllActive(): List<OfficeEntity>
}
