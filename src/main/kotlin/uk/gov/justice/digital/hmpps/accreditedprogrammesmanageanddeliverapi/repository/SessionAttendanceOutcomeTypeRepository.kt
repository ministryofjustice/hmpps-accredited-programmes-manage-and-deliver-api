package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceCode

@Repository
interface SessionAttendanceOutcomeTypeRepository : JpaRepository<SessionAttendanceOutcomeTypeEntity, SessionAttendanceCode> {
  fun findByCode(code: SessionAttendanceCode): SessionAttendanceOutcomeTypeEntity?
}
