package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.util.UUID

@Repository
interface ProgrammeGroupRepository :
  JpaRepository<ProgrammeGroupEntity, UUID>,
  JpaSpecificationExecutor<ProgrammeGroupEntity>,
  ProgrammeGroupRepositoryCustom {
  fun findByCode(code: String): ProgrammeGroupEntity?
  fun findByCodeAndRegionName(code: String, regionName: String): ProgrammeGroupEntity?
}
