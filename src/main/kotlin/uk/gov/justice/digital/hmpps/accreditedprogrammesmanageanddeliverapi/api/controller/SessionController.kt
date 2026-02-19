package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.UpdateSessionAttendeesRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.EditSessionAttendeesResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance.RecordSessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionDateAndTimeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.RegionService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.SessionService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.AuthenticationUtils
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.formatSessionNameForPage
import java.util.UUID

@Tag(
  name = "Session controller",
  description = "A series of endpoints to retrieve and manage session details and allocations",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class SessionController(
  private val sessionService: SessionService,
  private val authenticationUtils: AuthenticationUtils,
  private val userService: UserService,
  private val regionService: RegionService,
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
  ): ResponseEntity<EditSessionDateAndTimeResponse> = ResponseEntity.ok(sessionService.rescheduleSessions(sessionId, rescheduleSessionRequest))

  @Operation(
    tags = ["Session controller"],
    summary = "Endpoint to delete an individual session",
    operationId = "deleteSession",
    description = "Delete sessions",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully deleted session",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = String::class),
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
  ): ResponseEntity<String> {
    val deletedSession = sessionService.deleteSession(sessionId)
    return ResponseEntity.ok("${formatSessionNameForPage(deletedSession)} has been deleted.")
  }

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

  @Operation(
    tags = ["Session controller"],
    summary = "Retrieve session attendees by session ID",
    operationId = "retrieveSessionAttendees",
    description = "Retrieve the attendees for a session",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session attendees retrieved successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = EditSessionAttendeesResponse::class),
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
  @GetMapping("/bff/session/{sessionId}/attendees", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun retrieveSessionAttendees(@PathVariable sessionId: UUID): ResponseEntity<EditSessionAttendeesResponse> = ResponseEntity.ok(sessionService.getSessionAttendees(sessionId))

  @Operation(
    tags = ["Session controller"],
    summary = "Update session attendees",
    operationId = "updateAttendeesForSession",
    description = "Update the attendees for a session",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session attendees updated successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = String::class),
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
  @PutMapping("/session/{sessionId}/attendees", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun updateAttendeesForSession(
    @PathVariable @Parameter(description = "The unique session identifier") sessionId: UUID,
    @RequestBody @Valid updateAttendeesRequest: UpdateSessionAttendeesRequest,
  ): ResponseEntity<String> = ResponseEntity.ok(sessionService.updateSessionAttendees(sessionId, updateAttendeesRequest.referralIdList))

  @Operation(
    tags = ["Session controller"],
    summary = "Retrieve session facilitators by session ID",
    operationId = "retrieveSessionFacilitators",
    description = "Retrieve the facilitators for a session",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session facilitators retrieved successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = EditSessionFacilitatorsResponse::class),
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
  @GetMapping("/bff/session/{sessionId}/session-facilitators", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun retrieveSessionFacilitators(@PathVariable sessionId: UUID): ResponseEntity<EditSessionFacilitatorsResponse> {
    val username = authenticationUtils.getUsername()
    val (userRegion) = userService.getUserRegions(username)
    val regionFacilitators: MutableList<UserTeamMember> =
      regionService.getTeamMembersForPdu(userRegion.code).toMutableList()

    return ResponseEntity.ok(sessionService.getSessionFacilitators(sessionId, regionFacilitators))
  }

  @Operation(
    tags = ["Session controller"],
    summary = "Edit session facilitators of a session",
    operationId = "editSessionFacilitators",
    description = "Edit the facilitators for a session",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Session facilitators updated successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = String::class),
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
  @PutMapping(
    "/session/{sessionId}/session-facilitators",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun editSessionFacilitators(
    @PathVariable sessionId: UUID,
    @RequestBody @Valid @NotEmpty(message = "Session facilitators cannot be empty") editSessionFacilitatorsRequest: List<EditSessionFacilitatorRequest>,
  ): ResponseEntity<String> {
    sessionService.editSessionFacilitators(sessionId, editSessionFacilitatorsRequest)
    return ResponseEntity.ok("The people responsible for this session have been updated.")
  }

  @Operation(
    tags = ["Session controller"],
    summary = "Create attendance of a session",
    operationId = "createAttendance",
    description = "Create new attendance for a session",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Session attendance created successfully",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = SessionAttendance::class),
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
  @PostMapping(
    "/session/{sessionId}/attendance",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun createAttendance(
    @PathVariable sessionId: UUID,
    @RequestBody @Valid sessionAttendance: SessionAttendance,
  ): ResponseEntity<SessionAttendance> = ResponseEntity.status(HttpStatus.CREATED)
    .body(sessionService.saveSessionAttendance(sessionId, sessionAttendance))

  @Operation(
    tags = ["Session controller"],
    summary = "BFF endpoint to retrieve a record attendance for a session",
    operationId = "getSessionRecordAttendance",
    description = "Retrieve a record attendance for a session",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved record attendance details",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = RecordSessionAttendance::class),
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
  @GetMapping(
    "/bff/session/{sessionId}/record-attendance",
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun getGroupSessionRecordAttendance(
    @Parameter(description = "Filter by referral IDs. Can pass multiple values: ?referralId=referral-a&referralId=referral-b")
    @RequestParam(name = "referralId", required = false) referralIds: List<UUID>?,
    @PathVariable @Parameter(description = "Unique identifier of a session", required = true) sessionId: UUID,
  ): ResponseEntity<RecordSessionAttendance> = ResponseEntity.ok(sessionService.getRecordAttendanceBySessionId(sessionId, referralIds))
}
