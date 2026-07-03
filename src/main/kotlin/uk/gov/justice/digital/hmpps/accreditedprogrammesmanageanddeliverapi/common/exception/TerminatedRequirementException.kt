package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception

/**
 * Thrown when nDelius rejects appointment creation because the linked requirement
 * (or licence condition) has been terminated in nDelius.
 *
 * Extends [BusinessException] so today it flows through the existing global
 * exception handler unchanged (generic `Exception → HTTP 500` — same as any other
 * BusinessException). Callers that want to react to this specific failure can catch
 * the typed exception; everything else keeps working exactly as before.
 */
class TerminatedRequirementException(
  val requirementIds: List<String>,
  message: String,
  cause: Throwable? = null,
) : BusinessException(message, cause)
