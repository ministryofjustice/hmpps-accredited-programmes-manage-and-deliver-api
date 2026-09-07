package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService.Access
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.util.UUID

@Schema(description = "Details of a session")
data class Session(
  @get:Schema(description = "Unique identifier for a session", example = "0da0096f-8950-4bb9-9695-5e10a1f3a9c2")
  val id: UUID,

  @get:Schema(description = "Type of a session", example = "Group")
  val type: String,

  @get:Schema(description = "Name of a session", example = "Getting started")
  val name: String,

  @get:Schema(description = "Number of a session", example = "1")
  val number: Int,

  @get:Schema(description = "A list of referrals for a session")
  val referrals: List<Referral>,

  @get:Schema(description = "A flag if a session is a catchup or not", example = "false")
  val isCatchup: Boolean = false,

  @get:Schema(
    description = "The title of the page",
    required = true,
    example = "Attendance and notes for Getting started session",
  )
  val pageTitle: String,
)

fun SessionEntity.toApi(formatter: SessionNameFormatter, usernameAccessMap: Map<String, Access>) = Session(
  id = id!!,
  type = sessionType.value,
  name = moduleSessionTemplate.module.name,
  number = sessionNumber,
  referrals = attendees.map { attendee ->
    val access = usernameAccessMap[attendee.referral.crn]
    attendee.referral.toApi(
      isLimitedAccessOffender = access?.isLimitedAccessOffender,
      isExcluded = access?.isExcluded,
    )
  },
  isCatchup = isCatchup,
  pageTitle = "Delete ${formatter.format(this, SessionNameContext.Default)}",
)
