package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate
import java.util.UUID

@Repository
interface ProgrammeGroupRepository :
  JpaRepository<ProgrammeGroupEntity, UUID>,
  JpaSpecificationExecutor<ProgrammeGroupEntity> {
  fun findByCode(code: String): ProgrammeGroupEntity?
  fun findByCodeAndRegionName(code: String, regionName: String): ProgrammeGroupEntity?

  @Query(
    "SELECT DISTINCT p.probationDeliveryUnitName FROM ProgrammeGroupEntity p " +
      "WHERE p.regionName IN (:regionNames) " +
      "AND p.probationDeliveryUnitName IS NOT NULL " +
      "ORDER BY p.probationDeliveryUnitName",
  )
  fun findDistinctProbationDeliveryUnitNames(regionNames: Collection<String>): List<String>

  @Query(
    "SELECT DISTINCT p.deliveryLocationName FROM ProgrammeGroupEntity p " +
      "WHERE p.regionName IN (:regionNames) " +
      "AND p.probationDeliveryUnitName IN (:pdu) " +
      "AND p.deliveryLocationName IS NOT NULL " +
      "ORDER BY p.deliveryLocationName",
  )
  fun findDistinctDeliveryLocationNames(regionNames: Collection<String>, pdu: List<String>): List<String>

  /**
   * Finds groups whose first scheduled instance of a session, ignoring catch-ups, is between two dates (inclusive).
   * When [regionName] is null, groups in every region other than [excludedRegionNames] are returned.
   */
  @Query(
    value = """
      SELECT pg.id
      FROM programme_group pg
      JOIN session s ON s.programme_group_id = pg.id
      WHERE pg.deleted_at IS NULL
        AND s.module_session_template_id = :sessionTemplateId
        AND s.is_catchup = FALSE
        AND s.is_placeholder = FALSE
        AND (
          pg.region_name = CAST(:regionName AS TEXT)
          OR (CAST(:regionName AS TEXT) IS NULL AND pg.region_name NOT IN (:excludedRegionNames))
        )
      GROUP BY pg.id
      HAVING MIN(s.starts_at)::date BETWEEN :startDate AND :endDate
      """,
    nativeQuery = true,
  )
  fun findIdsBySessionFirstScheduledBetween(
    sessionTemplateId: UUID,
    startDate: LocalDate,
    endDate: LocalDate,
    regionName: String?,
    excludedRegionNames: Collection<String>,
  ): List<UUID>
}
