package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeleteSessionCaptionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.EditSessionAttendeesResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.fromDateTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionDateAndTimeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.toEditSessionFacilitator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toLocalTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toSessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Service
@Transactional
class SessionService(
  @Autowired
  private val sessionRepository: SessionRepository,
  @Autowired
  private val scheduleService: ScheduleService,
  @Autowired
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  @Autowired
  private val facilitatorService: FacilitatorService,

  @Autowired
  private val referralRepository: ReferralRepository,
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

  fun getRescheduleSessionDetails(sessionId: UUID): RescheduleSessionDetails {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    return RescheduleSessionDetails(
      sessionId = sessionId,
      sessionName = formatSessionName(session),
      previousSessionDateAndTime = formatPreviousSessionDateAndTime(session),
    )
  }

  private fun formatTime(dateTime: LocalDateTime): String {
    val hour = if (dateTime.hour == 0 || dateTime.hour == 12) 12 else dateTime.hour % 12
    val amPm = if (dateTime.hour < 12) "am" else "pm"
    val minutes = if (dateTime.minute == 0) "" else ":${dateTime.minute.toString().padStart(2, '0')}"
    return "$hour$minutes$amPm"
  }

  fun rescheduleSessions(sessionId: UUID, request: RescheduleSessionRequest): EditSessionDateAndTimeResponse {
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

      return EditSessionDateAndTimeResponse("The date and time and schedule have been updated.")
    }

    if (!isGroupSession) {
      request.sessionEndTime?.let { requestedEndTime ->
        session.endsAt = LocalDateTime.of(request.sessionStartDate, requestedEndTime.toLocalTime())
      }
    }
    return EditSessionDateAndTimeResponse("The date and time have been updated.")
  }

  fun getSession(sessionId: UUID): Session {
    val entity = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session with id $sessionId not found.")
    }

    return entity.toApi()
  }

  fun getSessionAttendees(sessionId: UUID): EditSessionAttendeesResponse {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }
    val groupMembers = programmeGroupMembershipRepository.findAllActiveByProgrammeGroupId(session.programmeGroup.id!!)
      .ifEmpty { throw NotFoundException("Cannot get attendees as there are currently no members allocated to group with id: ${session.programmeGroup.id!!}") }

    val sessionAttendees =
      groupMembers.map { groupMember -> groupMember.toSessionAttendee(session.attendees.map { session -> session.referralId }) }

    return EditSessionAttendeesResponse(
      sessionId = sessionId,
      sessionName = formatSessionName(session),
      sessionType = session.sessionType,
      isCatchup = session.isCatchup,
      attendees = sessionAttendees,
    )
  }

  fun getSessionFacilitators(
    sessionId: UUID,
    regionFacilitators: MutableList<UserTeamMember>,
  ): EditSessionFacilitatorsResponse {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    val sessionFacilitatorCodes = session.sessionFacilitators.map { it.facilitatorCode }

    return EditSessionFacilitatorsResponse(
      headingText = formatSessionName(session),
      facilitators = regionFacilitators.map { it.toEditSessionFacilitator(sessionFacilitatorCodes) },
    )
  }

  fun editSessionFacilitators(
    sessionId: UUID,
    editSessionFacilitatorsRequest: List<EditSessionFacilitatorRequest>,
  ) {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    val sessionFacilitators = editSessionFacilitatorsRequest.map { facilitatorRequest ->
      val facilitatorEntity = facilitatorService.findOrCreateFacilitator(facilitatorRequest)

      SessionFacilitatorEntity(
        facilitator = facilitatorEntity,
        session = session,
        facilitatorType = FacilitatorType.REGULAR_FACILITATOR,
      )
    }.toMutableSet()
    session.sessionFacilitators.clear()
    session.sessionFacilitators.addAll(sessionFacilitators)
    sessionRepository.save(session)
  }

  fun deleteSession(sessionId: UUID): DeleteSessionCaptionResponse {
    val sessionEntity = sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException(
      "Session with id $sessionId not found.",
    )
    val caption = getDeleteSessionResponseMessage(sessionEntity)
    scheduleService.removeNDeliusAppointments(sessionEntity.ndeliusAppointments.toList(), listOf(sessionEntity))
    sessionRepository.delete(sessionEntity)

    return DeleteSessionCaptionResponse(caption = caption)
  }

  fun updateSessionAttendees(sessionId: UUID, referralIds: List<UUID>): String {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    session.attendees.clear()

    val newAttendees = referralIds.map { referralId ->
      val referral = referralRepository.findById(referralId).orElseThrow {
        NotFoundException("Referral not found with id: $referralId")
      }
      AttendeeEntity(referral = referral, session = session)
    }

    session.attendees.addAll(newAttendees)
    sessionRepository.save(session)

    return "The date and time have been updated."
  }

  private fun getDeleteSessionResponseMessage(sessionEntity: SessionEntity): String {
    val sessionName = sessionEntity.sessionName.replace(" one-to-one", "")
    if (sessionEntity.moduleName == "Post-programme reviews") {
      return "${sessionEntity.attendees.first().personName}: post-programme review has been deleted"
    } else if (sessionEntity.sessionType == ONE_TO_ONE) {
      return "${sessionEntity.attendees.first().personName}: $sessionName ${sessionEntity.sessionNumber} one-to-one has been deleted."
    }

    return "$sessionName ${sessionEntity.sessionNumber} catch-up has been deleted."
  }

  private fun formatSessionName(session: SessionEntity): String {
    val baseName = session.moduleSessionTemplate.name

    // Session attendees could be null if we are editing a session before anyone has been allocated to a group.
    val name = if (session.sessionType == ONE_TO_ONE) {
      val attendee = session.attendees.firstOrNull()?.personName?.takeIf { it.isNotBlank() }
      attendee?.let { "$it: $baseName" } ?: baseName
    } else {
      baseName
    }

    return if (session.isCatchup) "$name catch-up" else name
  }

  private fun formatPreviousSessionDateAndTime(session: SessionEntity): String {
    val date = session.startsAt.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH))
    val startTime = formatTime(session.startsAt)
    val endTime = formatTime(session.endsAt)

    return "$date, $startTime to $endTime"
  }
}
