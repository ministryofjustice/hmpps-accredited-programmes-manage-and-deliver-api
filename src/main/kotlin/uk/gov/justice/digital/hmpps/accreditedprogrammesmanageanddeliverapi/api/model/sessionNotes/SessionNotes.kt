package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.sessionNotes

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Details of the notes for a session")
data class SessionNotes(
  @Schema(
    description = "The page title for the session notes page",
    example = "Alex River: Getting started 1 Introduction to Building Choices session notes",
  )
  val pageTitle: String,

  @Schema(
    description = "The name of the module the session belongs to",
    example = "Getting started",
  )
  val moduleName: String,

  @Schema(
    description = "The session number within the module",
    example = "1",
  )
  val sessionNumber: Int,

  @get:JsonProperty("lastUpdatedBy")
  @Schema(
    description = "The user that last updated the session notes",
    example = "John Smith",
  )
  val lastUpdatedBy: String? = null,

  @get:JsonProperty("lastUpdatedDate")
  @Schema(
    description = "The date the session notes were last updated",
    example = "19 March 2026",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  val lastUpdatedDate: LocalDate? = null,

  @Schema(
    description = "The unique identifier of the programme group",
    example = "d193bf89-c98b-4e92-b842-3c1b3e5f5e4a",
  )
  val groupId: UUID,

  @Schema(
    description = "The unique identifier of the session",
    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  )
  val sessionId: UUID,

  @get:JsonProperty("sessionDate")
  @Schema(
    description = "The date the session took place",
    example = "21 July 2025",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  val sessionDate: LocalDate? = null,

  @Schema(
    description = "The attendance status for the session",
    example = "Attended, failed to comply",
  )
  val sessionAttendance: String,

  @Schema(
    description = "The notes recorded for this session",
    example = "Participant engaged well.",
  )
  val sessionNotes: String?,
) {
  companion object {
    fun from(
      session: SessionEntity,
      referralId: UUID,
      sessionAttendance: String,
      pageTitle: String,
    ): SessionNotes {
      val attendance = session.attendances.filter { it.groupMembership.referralId == referralId }.maxByOrNull { it.createdAt } ?: throw NotFoundException("No session attendance found for referralId: $referralId")
      val latestSessionNotes = attendance.notesHistory.maxByOrNull { it.createdAt }
      return SessionNotes(
        pageTitle = pageTitle,
        moduleName = session.moduleName,
        sessionNumber = session.sessionNumber,
        lastUpdatedBy = attendance.createdBy,
        lastUpdatedDate = attendance.createdAt.toLocalDate(),
        groupId = session.programmeGroup.id!!,
        sessionId = session.id!!,
        sessionDate = session.startsAt.toLocalDate(),
        sessionAttendance = sessionAttendance,
        sessionNotes = latestSessionNotes?.notes,
      )
    }
  }
}
