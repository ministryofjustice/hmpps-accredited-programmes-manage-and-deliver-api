package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

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
