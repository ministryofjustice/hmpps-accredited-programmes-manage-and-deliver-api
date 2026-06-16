package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import java.util.UUID

@Repository
interface ModuleRepository : JpaRepository<ModuleEntity, UUID> {
  fun findByAccreditedProgrammeTemplateId(accreditedProgrammeTemplateId: UUID): MutableList<ModuleEntity>
}
