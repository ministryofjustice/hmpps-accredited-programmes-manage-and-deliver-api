package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
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
}
