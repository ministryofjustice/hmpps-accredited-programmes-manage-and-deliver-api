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

  @get:Schema(
    description = "Unique identifier of the session",
    example = "9f31c7c0-5a80-42ef-8b9a-7bf2deec09f0",
  )
  val sessionId: UUID,

  @get:Schema(
    description = "Ryan Hermiston: Getting started one-to-one",
    example = "Session 3 – Relapse Prevention",
  )
  val sessionName: String,

  @get:Schema(
    description = "Type of session being delivered",
    implementation = SessionType::class,
  )
  val sessionType: SessionType,

  @get:Schema(
    description = "Indicates whether this is a catch-up session",
    example = "false",
  )
  val isCatchup: Boolean,

  @get:Schema(
    description = "List of attendees for the session. May be empty if no members are currently allocated to the programme group.",
  )
  val attendees: List<EditSessionAttendee>,
) {

  @Schema(description = "Details about an attendee for a session")
  data class EditSessionAttendee(

    @get:Schema(
      description = "Full name of the attendee",
      example = "John Smith",
    )
    val name: String,

    @get:Schema(
      description = "Referral identifier associated with the attendee",
      example = "7d5bbfae-e3fe-4db4-9d3f-f41dcdafc8b3",
    )
    val referralId: UUID,

    @get:Schema(
      description = "CRN of the attendee",
      example = "X123456",
    )
    val crn: String,

    @get:Schema(
      description = "Whether this person is marked as currently attending the session",
      example = "true",
    )
    val currentlyAttending: Boolean,

    @get:Schema(description = "The boolean value of whether the group member has Limited Access Offender (LAO) status")
    var isLimitedAccessOffender: Boolean? = false,

    @get:Schema(description = "The boolean value of whether the group member details are excluded from viewing by the logged-in username")
    var isExcluded: Boolean? = false,
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
