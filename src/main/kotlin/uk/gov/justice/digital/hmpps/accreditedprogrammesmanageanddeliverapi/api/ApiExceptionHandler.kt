package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException

@RestControllerAdvice
class ApiExceptionHandler {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFoundException(exception: NotFoundException): ResponseEntity<ErrorResponse> {
    log.warn("Not found", exception)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND.value(),
          userMessage = "Not Found: ${exception.message}",
          developerMessage = exception.message,
        ),
      )
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  @ResponseStatus(BAD_REQUEST)
  fun handleEnumMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> = ResponseEntity.status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST.value(),
        userMessage = "Invalid value for parameter ${e.parameter.parameterName}",
        developerMessage = e.message,
      ),
    )

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(exception: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.warn("Access denied", exception)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN.value(),
          userMessage = "Access denied: ${exception.message}",
          developerMessage = exception.message,
        ),
      )
  }

  @ExceptionHandler(HandlerMethodValidationException::class)
  fun handleHandlerMethodValidationException(exception: HandlerMethodValidationException): ResponseEntity<ErrorResponse> {
    log.warn("Bad request", exception)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Bad request: ${exception.message}",
          developerMessage = exception.message,
        ),
      )
  }
}
