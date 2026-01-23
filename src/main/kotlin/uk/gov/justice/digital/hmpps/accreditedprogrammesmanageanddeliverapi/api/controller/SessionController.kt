package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.SessionService
import java.util.UUID

@Tag(
  name = "Session controller",
  description = "A series of endpoints to retrieve and manage session details and allocations",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class SessionController(
  private val sessionService: SessionService,
  private var sessionRepository: SessionRepository,
  private val scheduleService: ScheduleService,
) {

  @Operation(
    tags = ["Session controller"],
    summary = "Retrieve session details to edit",
    operationId = "retrieveSessionDetailsToEdit",
    description = "Retrieve the details for a session so they can be edited",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session details retrieved successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = EditSessionDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires role ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Session not found",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/session/{sessionId}/edit-session-date-and-time", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun retrieveSessionDetailsToEdit(
    @PathVariable @Parameter(description = "The unique session identifier") sessionId: UUID,
  ): ResponseEntity<EditSessionDetails> = ResponseEntity.ok(sessionService.getSessionDetailsToEdit(sessionId))

  @Operation(
    tags = ["Session controller"],
    summary = "Endpoint to delete an individual session",
    operationId = "deleteSession",
    description = "Delete sessions",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted session",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires role ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Session not found",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @DeleteMapping("/session/{sessionId}")
  fun deleteSession(@PathVariable sessionId: UUID): ResponseEntity<Unit> {
    val session =
      sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException("Session with id $sessionId not found.")
    scheduleService.removeNDeliusAppointments(session.ndeliusAppointments.toList(), listOf(session))
    sessionRepository.delete(session)

    return ResponseEntity.noContent().build()
  }
}
