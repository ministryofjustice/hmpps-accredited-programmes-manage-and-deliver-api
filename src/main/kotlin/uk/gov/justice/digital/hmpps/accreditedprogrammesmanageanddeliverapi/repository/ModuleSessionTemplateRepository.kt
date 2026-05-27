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
  fun findByModuleIdAndNotOneToOne(moduleId: UUID): List<UUID>

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

  @Query(
    value = """
      SELECT m.module_number AS moduleNumber,
             mst.session_number AS sessionNumber,
             mst.name AS sessionName
      FROM module_session_template mst
      JOIN module m ON mst.module_id = m.id
      JOIN accredited_programme_template apt ON m.accredited_programme_template_id = apt.id
      WHERE apt.name = 'Building Choices'
      ORDER BY m.module_number, mst.session_number
      """,
    nativeQuery = true,
  )
  fun getBuildingChoicesSessionColumns(): List<BuildingChoicesSessionColumnProjection>
}

interface BuildingChoicesSessionColumnProjection {
  val moduleNumber: Int
  val sessionNumber: Int
  val sessionName: String
}
