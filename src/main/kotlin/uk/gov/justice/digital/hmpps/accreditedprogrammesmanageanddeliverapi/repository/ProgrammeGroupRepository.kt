package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.util.UUID

interface ProgrammeGroupRepository : JpaRepository<ProgrammeGroupEntity, UUID> {

  fun findByCode(code: String): ProgrammeGroupEntity?
}
