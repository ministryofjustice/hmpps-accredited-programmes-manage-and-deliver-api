package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ModuleRepository : JpaRepository<ModuleEntity, UUID> {
  fun findByAccreditedProgrammeTemplateId(accreditedProgrammeTemplateId: UUID): MutableList<ModuleEntity>
}
