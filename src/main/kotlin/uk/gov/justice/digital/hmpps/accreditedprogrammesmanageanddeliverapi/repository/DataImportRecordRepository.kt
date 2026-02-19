package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DataImportRecordEntity
import java.util.UUID

interface DataImportRecordRepository : JpaRepository<DataImportRecordEntity, UUID> {

  fun existsByEntityTypeAndSourceId(entityType: String, sourceId: String): Boolean

  fun findByEntityTypeAndSourceId(entityType: String, sourceId: String): DataImportRecordEntity?
}
