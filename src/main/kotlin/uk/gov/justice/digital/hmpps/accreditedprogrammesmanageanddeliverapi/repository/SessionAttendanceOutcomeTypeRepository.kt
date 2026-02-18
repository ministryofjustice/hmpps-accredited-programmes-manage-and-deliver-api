package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity

@Repository
interface SessionAttendanceOutcomeTypeRepository : JpaRepository<SessionAttendanceOutcomeTypeEntity, String> {
  fun findByCode(code: String): SessionAttendanceOutcomeTypeEntity?
}
