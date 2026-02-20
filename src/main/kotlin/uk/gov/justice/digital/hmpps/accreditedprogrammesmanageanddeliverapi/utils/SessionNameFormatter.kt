package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType

/**
 * Represents the page context in which a session name will be displayed.
 * Pass the appropriate subtype to [SessionNameFormatter.format] to obtain
 * the correctly formatted string for that page.
 */
sealed class SessionNameContext {

  /**
   * Default formatting, used on pages such as:
   * - Edit Session Facilitators
   * - Delete Session
   */
  object Default : SessionNameContext()

  /**
   * Formatting for the Schedule Individual Session confirmation message,
   */
  object ScheduleIndividualSession : SessionNameContext()

  /**
   * Formatting for the Schedule Overview page.
   */
  object ScheduleOverview : SessionNameContext()

  /**
   * Formatting for the Sessions and Attendance page.
   *
   * @property sessionTemplate The module session template associated with the scheduled session.
   */
  data class SessionsAndAttendance(val sessionTemplate: ModuleSessionTemplateEntity) : SessionNameContext()

  /**
   * Formatting for the Session details page.
   */
  object SessionDetails : SessionNameContext()
}

/**
 * Component responsible for formatting [SessionEntity] instances
 * into human-readable session name strings for display in the UI.
 *
 * The format varies depending on the page being rendered, which is communicated
 * via a [SessionNameContext]. Call [format] with the appropriate context to obtain
 * the correctly formatted string.
 *
 * Example usage:
 * ```kotlin
 * sessionNameFormatter.format(session, SessionNameContext.Default)
 * sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(template))
 * ```
 */
@Component
class SessionNameFormatter {

  /**
   * Formats a [session] name according to the given [context].
   *
   * @param session The session entity to format.
   * @param context The page context that determines which formatting rules to apply.
   * @return A formatted session name string suitable for display on the target page.
   */
  fun format(session: SessionEntity, context: SessionNameContext): String = when (context) {
    is SessionNameContext.Default -> defaultFormatting(session)
    is SessionNameContext.ScheduleIndividualSession -> scheduleIndividualSession(session)
    is SessionNameContext.ScheduleOverview -> scheduleOverview(session)
    is SessionNameContext.SessionsAndAttendance -> sessionsAndAttendance(context.sessionTemplate, session)
    is SessionNameContext.SessionDetails -> sessionDetails(session)
  }

  /**
   * Default session name format, appending a catch-up suffix where applicable.
   *
   * - GROUP: `"<moduleName> <sessionNumber>[catch-up]"`
   * - ONE_TO_ONE: `"<personName>: <sessionName>[ catch-up]"`
   *
   * @throws IllegalArgumentException if the session is ONE_TO_ONE and has no attendees.
   */
  private fun defaultFormatting(session: SessionEntity): String {
    val catchupSuffix = if (session.isCatchup) " catch-up" else ""
    return when (session.sessionType) {
      SessionType.GROUP -> "${session.moduleName} ${session.sessionNumber}$catchupSuffix"

      SessionType.ONE_TO_ONE -> {
        requireNotNull(session.attendees.first()) { "Person name is required for individual sessions" }
        "${session.attendees.first().personName}: ${session.sessionName}$catchupSuffix"
      }
    }
  }

  /**
   * Formats a confirmation message shown after a session has been scheduled.
   *
   * - ONE_TO_ONE catch-up: `"<sessionName> catch-up for <personName> has been added."`
   * - ONE_TO_ONE: `"<sessionName> for <personName> has been added."`
   * - GROUP catch-up: `"<moduleName> <sessionNumber> catch-up has been added."`
   * - GROUP: `"<moduleName> <sessionNumber> has been added."`
   */
  private fun scheduleIndividualSession(session: SessionEntity): String = when {
    session.sessionType == SessionType.ONE_TO_ONE && session.isCatchup ->
      "${session.sessionName} catch-up for ${session.attendees.first().personName} has been added."

    session.sessionType == SessionType.ONE_TO_ONE ->
      "${session.sessionName} for ${session.attendees.first().personName} has been added."

    session.isCatchup ->
      "${session.moduleName} ${session.sessionNumber} catch-up has been added."

    else ->
      "${session.moduleName} ${session.sessionNumber} has been added."
  }

  /**
   * Formats the session name for the Sessions and Attendance page.
   *
   * - GROUP: `"<module.name> <sessionNumber>: <templateName>"`
   * - ONE_TO_ONE: `"<personName> (<crn>): <templateName>"`
   */
  private fun sessionsAndAttendance(
    sessionTemplate: ModuleSessionTemplateEntity,
    scheduledSession: SessionEntity,
  ): String = when (sessionTemplate.sessionType) {
    SessionType.GROUP -> "${sessionTemplate.module.name} ${scheduledSession.sessionNumber}: ${sessionTemplate.name}"
    SessionType.ONE_TO_ONE -> "${scheduledSession.attendees.first().personName} (${scheduledSession.attendees.first().referral.crn}): ${sessionTemplate.name}"
  }

  /**
   * Formats the session name for the Schedule Overview page.
   *
   * - Pre-group sessions: returns [SessionEntity.moduleName] as-is.
   * - Post-programme sessions: `"<sessionName> deadline"`
   * - ONE_TO_ONE: `"<moduleName> one-to-ones"`
   * - GROUP: `"<moduleName> <sessionNumber>"`
   */
  private fun scheduleOverview(session: SessionEntity): String = when {
    session.moduleName.startsWith("Pre-group") -> session.moduleName
    session.moduleName.startsWith("Post-programme") -> "${session.sessionName} deadline"
    session.sessionType == SessionType.ONE_TO_ONE -> "${session.moduleName} one-to-ones"
    else -> "${session.moduleName} ${session.sessionNumber}"
  }

  /**
   * Formats the session name for use as a page title on session details page.
   *
   * - GROUP: `"<module.name> <sessionNumber>: <templateName>"`
   * - ONE_TO_ONE: `"<personName>: <templateName>"`
   */
  private fun sessionDetails(session: SessionEntity): String {
    val catchupSuffix = if (session.isCatchup) " catch-up" else ""
    return when (session.sessionType) {
      SessionType.GROUP -> "${session.moduleSessionTemplate.module.name} ${session.sessionNumber}: ${session.moduleSessionTemplate.name}$catchupSuffix"

      SessionType.ONE_TO_ONE -> {
        requireNotNull(session.attendees.first()) { "Person name is required for individual sessions" }
        "${session.attendees.first().personName}: ${session.moduleSessionTemplate.name}$catchupSuffix"
      }
    }
  }
}
