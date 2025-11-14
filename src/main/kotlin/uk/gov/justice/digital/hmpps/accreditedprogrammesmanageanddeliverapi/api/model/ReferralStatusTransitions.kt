package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Status transition information for the update status form in the M&D UI")
data class ReferralStatusTransitions(
  @field:Schema(description = "The current status information")
  @get:JsonProperty("currentStatus", required = true)
  val currentStatus: CurrentStatus,

  @field:Schema(description = "List of transition statuses")
  @get:JsonProperty("availableStatuses", required = true)
  val availableStatuses: List<ReferralStatus>,
)

@Schema(description = "Form data for the update status form in the M&D UI")
data class CurrentStatus(
  @field:Schema(description = "The id of the status description")
  @get:JsonProperty("statusDescriptionId", required = true)
  val statusDescriptionId: UUID,

  @field:Schema(description = "Title of the status description")
  @get:JsonProperty("title", required = true)
  val title: String,

  @field:Schema(description = "The display colour of the status tag")
  @get:JsonProperty("tagColour", required = true)
  val tagColour: String,

  @Schema(description = "The name of the person that updated the last status")
  @get:JsonProperty("updatedByName", required = true)
  val updatedByName: String,

  @Schema(description = "The date that the status was last updated")
  @get:JsonProperty("createdAt", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val createdAt: LocalDate,
)

fun ReferralStatusHistoryEntity.toCurrentStatus() = CurrentStatus(
  statusDescriptionId = referralStatusDescription.id,
  title = referralStatusDescription.description,
  tagColour = referralStatusDescription.labelColour,
  updatedByName = createdBy,
  createdAt = createdAt.toLocalDate(),
)
