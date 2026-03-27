package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.util.UUID

@Repository
interface SessionAttendanceRepository : JpaRepository<SessionAttendanceEntity, UUID> {

  fun findByAttendee(attendee: AttendeeEntity): MutableList<SessionAttendanceEntity>
  fun findTopByAttendeeIdOrderByRecordedAtDesc(attendeeId: UUID): SessionAttendanceEntity?

  fun findTopByAttendeeIdOrderByCreatedAtDesc(attendeeId: UUID): SessionAttendanceEntity?
}
