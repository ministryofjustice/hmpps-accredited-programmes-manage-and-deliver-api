package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.util.UUID

@Repository
interface SessionRepository : JpaRepository<SessionEntity, UUID> {

  fun findByModuleSessionTemplateIdAndProgrammeGroupId(moduleSessionTemplateId: UUID, programmeGroupId: UUID): List<SessionEntity>
}
