package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import java.util.UUID

interface ModuleSessionTemplateRepository : JpaRepository<ModuleSessionTemplateEntity, UUID> {
  fun findByModuleId(moduleId: UUID): List<ModuleSessionTemplateEntity>
  fun findByName(name: String): ModuleSessionTemplateEntity?

  @Query(
    "SELECT DISTINCT m.id FROM ModuleSessionTemplateEntity m " +
      "WHERE m.module.id = :moduleId " +
      "AND m.sessionType != 'ONE_TO_ONE'",
  )
  fun findByModuleIdAndNotCatchUp(moduleId: UUID): List<UUID>

  @Query(
    "SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ModuleSessionTemplateEntity m " +
      "WHERE m.module.id = :moduleId " +
      "AND m.name = 'Pre-group one-to-one'",
  )
  fun isAPreGroupSession(moduleId: UUID): Boolean

  @Query(
    "SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ModuleSessionTemplateEntity m " +
      "WHERE m.module.id = :moduleId " +
      "AND m.name = 'Post-programme review'",
  )
  fun isAPostProgrammeSession(moduleId: UUID): Boolean
}
