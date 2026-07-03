package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

/**
 * Detects nDelius "terminated requirement" failures from the response body of a failed
 * `POST /appointments` call.
 *
 * The nDelius integration returns HTTP 400 with a body of the form
 * `{"status":400,"message":"Invalid Requirement IDs: [1503618208]"}` when the linked
 * requirement (or licence condition) has been terminated in nDelius. The requirement
 * itself still exists (a GET returns 200), it just cannot accept new appointments.
 *
 * NB: this detection is a substring/regex match against a free-text message. If nDelius
 * ever changes the wording, detection silently stops — the failure would still be
 * counted via the generic `Appointment.create-nDelius.failure` telemetry event, so a
 * drop-to-zero in the terminated-requirement dimension against a stable failure count
 * is the tripwire.
 */
object TerminatedRequirementDetector {
  private const val MARKER = "Invalid Requirement IDs"
  private val ID_LIST_REGEX = Regex("""Invalid Requirement IDs:\s*\[([\d,\s]*)]""")

  fun isTerminated(body: String?): Boolean = body?.contains(MARKER) == true

  fun extractRequirementIds(body: String?): List<String> = ID_LIST_REGEX.find(body ?: "")
    ?.groupValues?.get(1)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?: emptyList()
}
