package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.util.UUID

@Repository
interface ProgrammeGroupRepository :
  JpaRepository<ProgrammeGroupEntity, UUID>,
  JpaSpecificationExecutor<ProgrammeGroupEntity> {
  fun findByCode(code: String): ProgrammeGroupEntity?
  fun findByCodeAndRegionName(code: String, regionName: String): ProgrammeGroupEntity?

  @Query(
    "SELECT DISTINCT p.probationDeliveryUnitName FROM ProgrammeGroupEntity p " +
      "WHERE p.regionName = :regionName " +
      "AND p.probationDeliveryUnitName IS NOT NULL",
  )
  fun findDistinctProbationDeliveryUnitNames(regionName: String): List<String>

  @Query(
    "SELECT DISTINCT p.deliveryLocationName FROM ProgrammeGroupEntity p " +
      "WHERE p.regionName = :regionName " +
      "AND p.probationDeliveryUnitName = :pdu " +
      "AND p.deliveryLocationName IS NOT NULL",
  )
  fun findDistinctDeliveryLocationNames(regionName: String, pdu: String): List<String>
}
