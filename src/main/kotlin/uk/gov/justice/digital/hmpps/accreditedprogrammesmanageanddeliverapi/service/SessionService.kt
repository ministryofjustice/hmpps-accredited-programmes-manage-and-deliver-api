package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.fromDateTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
@Transactional(readOnly = true)
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
}
