package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeleteSessionCaptionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
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
    summary = "Retrieve details for rescheduling a session",
    operationId = "getRescheduleSessionDetails",
    description = "Retrieve the details for a session so they can be rescheduled",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Reschedule session details retrieved successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = RescheduleSessionDetails::class),
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
  @GetMapping(
    "/bff/session/{sessionId}/edit-session-date-and-time/reschedule",
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun getRescheduleSessionDetails(
    @PathVariable @Parameter(description = "The unique session identifier") sessionId: UUID,
  ): ResponseEntity<RescheduleSessionDetails> = ResponseEntity.ok(sessionService.getRescheduleSessionDetails(sessionId))

  @Operation(
    tags = ["Session controller"],
    summary = "Endpoint to delete an individual session",
    operationId = "deleteSession",
    description = "Delete sessions",
    responses = [
      ApiResponse(
        responseCode = "200",
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
  fun deleteSession(
    @PathVariable sessionId: UUID,
  ): ResponseEntity<DeleteSessionCaptionResponse> = ResponseEntity.ok(sessionService.deleteSession(sessionId))

  @Operation(
    tags = ["Session controller"],
    summary = "Reschedule a session and optionally subsequent group sessions",
    operationId = "rescheduleSession",
    description = "Update the start and end time of a session, and optionally update subsequent group sessions in the same programme group.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session rescheduled successfully",
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
  @PutMapping("session/{sessionId}/reschedule", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun rescheduleSession(
    @PathVariable @Parameter(description = "The unique session identifier") sessionId: UUID,
    @RequestBody rescheduleSessionRequest: RescheduleSessionRequest,
  ): ResponseEntity<String> = ResponseEntity.ok(sessionService.rescheduleSessions(sessionId, rescheduleSessionRequest))

  @Operation(
    tags = ["Session controller"],
    summary = "Retrieve session by an ID",
    operationId = "retrieveSessionById",
    description = "Retrieve the details for a session ",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session details retrieved successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = Session::class),
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
  @GetMapping("/bff/session/{sessionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun retrieveSessionById(
    @PathVariable @Parameter(description = "The unique session identifier") sessionId: UUID,
  ): ResponseEntity<Session> = ResponseEntity.ok(sessionService.getSession(sessionId))
}
