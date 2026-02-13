package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.formatSessionNameForPage
import java.util.UUID

@Schema(description = "Details of a session")
data class Session(
  @Schema(description = "Unique identifier for a session", example = "0da0096f-8950-4bb9-9695-5e10a1f3a9c2")
  val id: UUID,

  @Schema(description = "Type of a session", example = "Group")
  val type: String,

  @Schema(description = "Name of a session", example = "Getting started")
  val name: String,

  @Schema(description = "Number of a session", example = "1")
  val number: Int,

  @Schema(description = "A list of referrals for a session")
  val referrals: List<Referral>,

  @Schema(description = "A flag if a session is a catchup or not", example = "false")
  val isCatchup: Boolean = false,

  @Schema(
    description = "The title of the page",
    required = true,
    example = "Attendance and notes for Getting started session",
  )
  val pageTitle: String,
)

fun SessionEntity.toApi() = Session(
  id = id!!,
  type = sessionType.value,
  name = moduleSessionTemplate.module.name,
  number = sessionNumber,
  referrals = attendees.map { it.referral.toApi() },
  isCatchup = isCatchup,
  pageTitle = "Delete ${formatSessionNameForPage(this)}",
)
