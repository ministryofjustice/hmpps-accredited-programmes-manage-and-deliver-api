package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import java.util.UUID

interface ModuleSessionTemplateRepository : JpaRepository<ModuleSessionTemplateEntity, UUID> {
  fun findByModuleId(moduleId: UUID): List<ModuleSessionTemplateEntity>
  fun findByName(name: String): ModuleSessionTemplateEntity?
}
