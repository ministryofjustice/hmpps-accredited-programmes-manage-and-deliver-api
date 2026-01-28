package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.fromDateTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toLocalTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
@Transactional
class SessionService(
  private val sessionRepository: SessionRepository,
) {

  fun getSessionDetailsToEdit(sessionId: UUID): EditSessionDetails {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    return EditSessionDetails(
      sessionId = sessionId,
      groupCode = session.programmeGroup.code,
      sessionName = "${session.moduleSessionTemplate.module.name} ${session.sessionNumber}",
      sessionDate = session.startsAt.format(DateTimeFormatter.ofPattern("d/M/yyyy")),
      sessionStartTime = fromDateTime(session.startsAt),
      sessionEndTime = fromDateTime(session.endsAt),
    )
  }

  fun rescheduleSessions(sessionId: UUID, request: RescheduleSessionRequest): String {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    val requestedStartsAt = LocalDateTime.of(request.sessionStartDate, request.sessionStartTime.toLocalTime())
    val startOffset = Duration.between(session.startsAt, requestedStartsAt)

    session.startsAt = requestedStartsAt
    session.endsAt = session.endsAt.plus(startOffset)

    val isGroupSession = session.sessionType == SessionType.GROUP
    val shouldShiftOtherGroupSessions = request.rescheduleOtherSessions && isGroupSession

    if (shouldShiftOtherGroupSessions) {
      val subsequentGroupSessions = session.programmeGroup.sessions
        .asSequence()
        .filter { it.sessionType == SessionType.GROUP }
        .filter { it.startsAt.isAfter(session.startsAt) }
        .toList()

      subsequentGroupSessions.forEach { subsequentSession ->
        subsequentSession.startsAt = subsequentSession.startsAt.plus(startOffset)
        subsequentSession.endsAt = subsequentSession.endsAt.plus(startOffset)
      }

      return "The date and time and schedule have been updated."
    }

    if (!isGroupSession) {
      request.sessionEndTime?.let { requestedEndTime ->
        session.endsAt = LocalDateTime.of(request.sessionStartDate, requestedEndTime.toLocalTime())
      }
    }
    return "The date and time have been updated."
  }
}
