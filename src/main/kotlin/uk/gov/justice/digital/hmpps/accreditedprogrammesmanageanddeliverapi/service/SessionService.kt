package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.EditSessionAttendeesResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.fromDateTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance.Option
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance.RecordSessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance.SessionAttendancePerson
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionDateAndTimeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.toEditSessionFacilitator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toLocalTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toSessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toUpdateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.formatSessionNameForPage
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.collections.find

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
  @Autowired
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  @Autowired
  private val sessionAttendanceOutcomeTypeRepository: SessionAttendanceOutcomeTypeRepository,
) {

  fun getSessionDetailsToEdit(sessionId: UUID): EditSessionDetails {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    return EditSessionDetails(
      sessionId = sessionId,
      groupCode = session.programmeGroup.code,
      sessionName = formatSessionNameForPage(session),
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
      sessionName = formatSessionNameForPage(session),
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

      (subsequentGroupSessions + session).forEach {
        updateNDeliusAppointmentsForSession(it)
      }

      return EditSessionDateAndTimeResponse("The date and time and schedule have been updated.")
    }

    if (!isGroupSession) {
      request.sessionEndTime?.let { requestedEndTime ->
        session.endsAt = LocalDateTime.of(request.sessionStartDate, requestedEndTime.toLocalTime())
      }
    }

    updateNDeliusAppointmentsForSession(session)

    return EditSessionDateAndTimeResponse("The date and time have been updated.")
  }

  fun updateNDeliusAppointmentsForSession(session: SessionEntity) {
    if (session.ndeliusAppointments.isEmpty()) return

    val updateRequests = session.ndeliusAppointments.map { it.toUpdateAppointmentRequest() }

    when (
      val response = nDeliusIntegrationApiClient.updateAppointmentsInDelius(UpdateAppointmentsRequest(updateRequests))
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to update appointments with reason: ${response.getErrorMessage()}")
        throw BusinessException("Failure to update appointments", response.toException())
      }

      is ClientResult.Failure.Other -> {
        log.warn(
          "Failure to update appointments - Service: ${response.serviceName}, Exception: ${response.exception.message}",
          response.exception,
        )
        throw BusinessException(
          "Failure to update appointments in nDelius: ${response.exception.message}",
          response.exception,
        )
      }

      is ClientResult.Success -> {
        log.info("${updateRequests.size} appointments updated in nDelius for session with id: ${session.id}")
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(SessionService::class.java)
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
      groupMembers.map { groupMember -> groupMember.toSessionAttendee(session.attendees.map(AttendeeEntity::referralId)) }

    return EditSessionAttendeesResponse(
      sessionId = sessionId,
      sessionName = formatSessionNameForPage(session),
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
      pageTitle = "Edit ${formatSessionNameForPage(session)}",
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

  // Deletes session and returns the deleted entity and a properly formatted session name as a string
  // e.g. 'Getting started 1 catch-up'
  fun deleteSession(sessionId: UUID): Pair<SessionEntity, String> {
    val sessionEntity = sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException(
      "Session with id $sessionId not found.",
    )
    // We have to format the session name here before we delete it as there are some lazily loaded entities that will be removed after session deletion.
    val formattedSessionName = formatSessionNameForPage(sessionEntity)
    scheduleService.removeNDeliusAppointments(sessionEntity.ndeliusAppointments.toList(), listOf(sessionEntity))
    sessionRepository.delete(sessionEntity)
    return sessionEntity to formattedSessionName
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

  fun saveSessionAttendance(sessionId: UUID, sessionAttendance: SessionAttendance): SessionAttendance {
    val session = sessionRepository.findById(sessionId).orElseThrow {
      NotFoundException("Session not found with id: $sessionId")
    }

    val sessionAttendanceEntities = getSessionAttendanceFromAttendees(sessionAttendance.attendees, session)
    session.attendances.addAll(sessionAttendanceEntities)
    sessionRepository.save(session)

    val attendeesWithNotes = sessionAttendance.attendees.filter { !it.sessionNotes.isNullOrBlank() }
    if (attendeesWithNotes.isNotEmpty()) {
      val updateAppointmentRequests = attendeesWithNotes.mapNotNull { attendeeWithNotes ->
        val referralId = attendeeWithNotes.referralId
        val nDeliusAppointment = session.ndeliusAppointments.find { it.referral.id == referralId }
        nDeliusAppointment?.toUpdateAppointmentRequest(attendeeWithNotes.sessionNotes)
      }

      if (updateAppointmentRequests.isNotEmpty()) {
        nDeliusIntegrationApiClient.updateAppointmentsInDelius(UpdateAppointmentsRequest(updateAppointmentRequests))
      }
    }

    sessionAttendance.responseMessage = "Attendance saved for session $sessionId"

    return sessionAttendance
  }

  fun getRecordAttendanceBySessionId(sessionId: UUID, referralIds: List<UUID>?): RecordSessionAttendance {
    val session = sessionRepository.findById(sessionId)
      .orElseThrow { NotFoundException("Session not found with id: $sessionId") }
    val group = session.programmeGroup

    val outcomeOptions = sessionAttendanceOutcomeTypeRepository.findAll().map { outcomeType ->
      getOptionFromOutcome(outcomeType)
    }

    val filteredAttendees = if (referralIds.isNullOrEmpty()) {
      session.attendees
    } else {
      session.attendees.filter { attendee ->
        referralIds.any { referralId ->
          attendee.referralId == referralId
        }
      }
    }

    return RecordSessionAttendance(
      sessionTitle = session.sessionName,
      groupRegionName = group.regionName,
      people = filteredAttendees.map { attendee ->
        SessionAttendancePerson(
          referralId = attendee.referralId,
          name = attendee.personName,
          crn = attendee.referral.crn,
          attendance = getSessionAttendanceText(session.attendances, attendee),
          options = outcomeOptions,
        )
      }.toList(),
    )
  }

  private fun getOptionFromOutcome(outcome: SessionAttendanceOutcomeTypeEntity): Option = when (outcome.code) {
    "ATTC" -> Option("Yes - attended", null, outcome.code)
    "AFTC" -> Option("Attended but failed to comply", "For example, they could not participate because of drug or alcohol use", outcome.code)
    "UAAB" -> Option("No - did not attend", null, outcome.code)
    else -> Option(outcome.description ?: outcome.code, null, outcome.code)
  }

  private fun getSessionAttendanceFromAttendees(
    attendees: List<SessionAttendee>,
    session: SessionEntity,
  ): List<SessionAttendanceEntity> {
    val programmeGroupId = session.programmeGroup.id!!
    val recordedByFacilitator =
      session.sessionFacilitators.find { it.facilitatorType == FacilitatorType.LEAD_FACILITATOR }?.facilitator
        ?: throw BusinessException("Lead facilitator not found for session: ${session.id}")

    return attendees.map { attendee ->
      val referralId = attendee.referralId
      val groupMembershipEntity =
        programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referralId, programmeGroupId)
          ?: throw NotFoundException(
            "Programme group membership not found with referralId: $referralId and programmeGroupId: $programmeGroupId",
          )

      val outcomeType = sessionAttendanceOutcomeTypeRepository.findByCode(attendee.outcomeCode)
        ?: throw NotFoundException("Session attendance outcome type not found with code: ${attendee.outcomeCode}")

      attendee.toEntity(session, groupMembershipEntity, recordedByFacilitator, outcomeType)
    }.toList()
  }

  private fun formatPreviousSessionDateAndTime(session: SessionEntity): String {
    val date = session.startsAt.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH))
    val startTime = formatTime(session.startsAt)
    val endTime = formatTime(session.endsAt)

    return "$date, $startTime to $endTime"
  }

  private fun getSessionAttendanceText(attendances: Set<SessionAttendanceEntity>, attendee: AttendeeEntity): String? {
    val attendance = attendances.find { it.groupMembership.referral.id == attendee.referralId }

    if (attendance == null) {
      return null
    }

    var attendanceText: String

    if (attendance.outcomeType.attendance != null && attendance.outcomeType.attendance == true) {
      attendanceText = "Attended"
    } else {
      return "Did not attend"
    }

    if (!attendance.outcomeType.compliant) {
      return "$attendanceText but failed to comply"
    }

    return attendanceText
  }
}
