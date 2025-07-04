package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception

/**
 * Thrown from domain logic to indicate that a business rule has been violated in a way that prevents
 * a request completing normally.
 */
class BusinessException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)
