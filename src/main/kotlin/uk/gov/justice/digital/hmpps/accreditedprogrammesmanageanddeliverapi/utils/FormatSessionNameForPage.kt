package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType

/**
 * Formats the session name for UI display according to specific business rules.
 * The format varies based on session type, catchup status, and module type.
 *
 * Pages this function can be used on (please add to this list if implemented for another page):
 * <ul>
 *   <li>Edit Session Facilitators</li>
 *   <li>Delete Session</li>
 * </ul>
 *
 * @param session The session entity to format
 * @return Formatted session name string
 */
fun formatSessionNameForPage(session: SessionEntity): String {
  val catchupSuffix = if (session.isCatchup) " catch-up" else ""

  return when (session.sessionType) {
    SessionType.GROUP -> {
      "${session.moduleName} ${session.sessionNumber}$catchupSuffix"
    }

    SessionType.ONE_TO_ONE -> {
      val attendees = session.attendees
      requireNotNull(attendees.first()) { "Person name is required for individual sessions" }
      "${attendees.first().personName}: ${session.sessionName}$catchupSuffix"
    }
  }
}

fun formatSessionNameForScheduleIndividualSession(session: SessionEntity): String = when {
  session.sessionType == SessionType.ONE_TO_ONE && session.isCatchup ->
    "${session.sessionName} catch-up for ${session.attendees.first().personName} has been added."

  session.sessionType == SessionType.ONE_TO_ONE ->
    "${session.sessionName} for ${session.attendees.first().personName} has been added."

  session.isCatchup ->
    "${session.moduleName} ${session.sessionNumber} catch-up has been added."

  else ->
    "${session.moduleName} ${session.sessionNumber} has been added."
}

fun formatSessionNameForModuleSessionPage(
  sessionTemplate: ModuleSessionTemplateEntity,
  scheduledSession: SessionEntity,
): String = when (sessionTemplate.sessionType) {
  SessionType.GROUP -> "${sessionTemplate.module.name} ${scheduledSession.sessionNumber}: ${sessionTemplate.name}"
  SessionType.ONE_TO_ONE -> "${scheduledSession.attendees.first().personName} (${scheduledSession.attendees.first().referral.crn}): ${sessionTemplate.name}"
}

fun formatSessionNameForScheduleOverview(session: SessionEntity) = when {
  session.moduleName.startsWith("Pre-group") -> session.moduleName

  session.moduleName.startsWith("Post-programme") -> "${session.sessionName} deadline"

  session.sessionType == SessionType.ONE_TO_ONE -> "${session.moduleName} one-to-ones"

  else -> {
    "${session.moduleName} ${session.sessionNumber}"
  }
}
