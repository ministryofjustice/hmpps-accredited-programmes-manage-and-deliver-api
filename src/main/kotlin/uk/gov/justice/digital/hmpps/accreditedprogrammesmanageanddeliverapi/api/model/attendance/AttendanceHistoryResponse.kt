package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Response containing attendance history for a referral")
data class AttendanceHistoryResponse(
  @Schema(
    description = "The full name of the person",
    example = "Alex River",
  )
  val popName: String,

  @Schema(
    description = "The group code of the group the person is currently allocated to ",
    example = "abc123",
  )
  val currentlyAllocatedGroupCode: String? = null,

  @Schema(
    description = "The unique identifier of the group the person is currently allocated to",
    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  )
  val currentlyAllocatedGroupId: UUID? = null,

  @Schema(description = "List of sessions with attendance information")
  val attendanceHistory: List<AttendanceHistorySession> = emptyList(),
)

@Schema(description = "Session attendance information")
data class AttendanceHistorySession(
  @Schema(
    description = "The unique identifier of the session",
    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  )
  val sessionId: UUID,

  @Schema(
    description = "The name of the session",
    example = "Pre-group one-to-one",
  )
  val sessionName: String,

  @Schema(
    description = "The group code for the session",
    example = "abc123",
  )
  val groupCode: String,

  @Schema(
    description = "The name of the person",
    example = "Alex River",
  )
  val popName: String,

  @Schema(
    description = "The group id for the session",
    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  )
  val groupId: UUID? = null,

  @Schema(
    description = "The date of the session",
    example = "11 July 2025",
  )
  val date: String,

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  @Schema(
    description = "The unformatted date of the session for sorting",
  )
  val unformattedDate: LocalDateTime,

  @Schema(
    description = "The time range of the session",
    example = "10:30am to 11am",
  )
  val time: String,

  @Schema(
    description = "The attendance status for the session",
    example = "Attended",
  )
  val attendanceStatus: String,

  @Schema(
    description = "Whether session notes exist for this attendance",
    example = "true",
  )
  val hasNotes: Boolean,

  @Schema(
    description = "Whether the session is a catch-up session",
    example = "true",
  )
  val isCatchup: Boolean,
)
