package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.EditSessionAttendeesResponse.EditSessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import java.util.UUID

@Schema(
  description = "Response representing attendees for a specific programme session",
)
data class EditSessionAttendeesResponse(

  @Schema(
    description = "Unique identifier of the session",
    example = "9f31c7c0-5a80-42ef-8b9a-7bf2deec09f0",
  )
  val sessionId: UUID,

  @Schema(
    description = "Ryan Hermiston: Getting started one-to-one",
    example = "Session 3 – Relapse Prevention",
  )
  val sessionName: String,

  @Schema(
    description = "Type of session being delivered",
    implementation = SessionType::class,
  )
  val sessionType: SessionType,

  @Schema(
    description = "Indicates whether this is a catch-up session",
    example = "false",
  )
  val isCatchup: Boolean,

  @Schema(
    description = "List of attendees for the session",
  )
  val attendees: List<EditSessionAttendee>,
) {

  @Schema(description = "Details about an attendee for a session")
  data class EditSessionAttendee(

    @Schema(
      description = "Full name of the attendee",
      example = "John Smith",
    )
    val name: String,

    @Schema(
      description = "Referral identifier associated with the attendee",
      example = "7d5bbfae-e3fe-4db4-9d3f-f41dcdafc8b3",
    )
    val referralId: UUID,

    @Schema(
      description = "CRN of the attendee",
      example = "X123456",
    )
    val crn: String,

    @Schema(
      description = "Whether this person is marked as currently attending the session",
      example = "true",
    )
    val currentlyAttending: Boolean,
  )
}

fun ProgrammeGroupMembershipEntity.toSessionAttendee(
  sessionAttendees: List<UUID>,
) = EditSessionAttendee(
  name = personName,
  referralId = referralId,
  crn = crn,
  currentlyAttending = sessionAttendees.contains(referralId),
)
