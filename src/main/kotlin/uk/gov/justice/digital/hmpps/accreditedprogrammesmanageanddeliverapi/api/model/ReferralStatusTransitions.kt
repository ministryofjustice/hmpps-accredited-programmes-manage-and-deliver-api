package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusTransitionEntity
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

  @field:Schema(description = "Suggested status object for the UI to potentially display")
  @get:JsonProperty("suggestedStatus", required = false)
  val suggestedStatus: SuggestedStatus? = null,

  @field:Schema(description = "The details of the group that the user is currently allocated to")
  @get:JsonProperty("currentGroupDetails", required = false)
  val currentGroupDetails: CurrentGroupDetails? = null,
)

@Schema(description = "Status transition information for the Remove Referral from Group form in the M&D UI")
data class RemoveReferralFromGroupStatusTransitions(
  @field:Schema(description = "The current status information")
  @get:JsonProperty("currentStatus", required = true)
  val currentStatus: CurrentStatus,

  @field:Schema(description = "List of transition statuses")
  @get:JsonProperty("availableStatuses", required = true)
  val availableStatuses: List<ReferralStatus>,

  @field:Schema(description = "Suggested status object for the UI to potentially display")
  @get:JsonProperty("suggestedStatus", required = false)
  val suggestedStatus: SuggestedStatus? = null,

  @field:Schema(description = "The details of the group that the user is currently allocated to")
  @get:JsonProperty("currentGroupDetails", required = false)
  val currentGroupDetails: CurrentGroupDetails? = null,

) {
  companion object {
    fun from(referralStatusTransitions: ReferralStatusTransitions): RemoveReferralFromGroupStatusTransitions = RemoveReferralFromGroupStatusTransitions(
      currentStatus = referralStatusTransitions.currentStatus,
      availableStatuses = referralStatusTransitions.availableStatuses,
      suggestedStatus = referralStatusTransitions.suggestedStatus,
      currentGroupDetails = referralStatusTransitions.currentGroupDetails,
    )
  }
}

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

fun ReferralStatusTransitionEntity.toSuggestedStatus(): SuggestedStatus {
  val labelText = when (fromStatusStatusName) {
    "Scheduled" -> "is allocated to"
    "On programme" -> "has started Building Choices"
    else -> ""
  }
  return SuggestedStatus(
    labelText = labelText,
    name = toStatusStatusName,
    statusDescriptionId = id,
  )
}

data class SuggestedStatus(
  @field:Schema(description = "The label text to be displayed on the UI")
  @get:JsonProperty("labelText", required = true)
  val labelText: String,
  @field:Schema(description = "Name of the status description")
  @get:JsonProperty("name", required = true)
  val name: String,
  @field:Schema(description = "The id of the status description")
  @get:JsonProperty("statusDescriptionId", required = true)
  val statusDescriptionId: UUID,
)

data class CurrentGroupDetails(
  @Schema(
    example = "ABC111",
    required = false,
    description = "The code of the currently allocated group",
  )
  @get:JsonProperty("currentlyAllocatedGroupCode", required = false)
  val currentlyAllocatedGroupCode: String?,

  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = false,
    description = "The unique code of the currently allocated group",
  )
  @get:JsonProperty("currentlyAllocatedGroupId", required = false)
  val currentlyAllocatedGroupId: UUID?,
)

fun ProgrammeGroupMembershipEntity.toCurrentGroupDetails() = CurrentGroupDetails(
  currentlyAllocatedGroupCode = groupCode,
  currentlyAllocatedGroupId = groupId,
)

fun ReferralStatusHistoryEntity.toCurrentStatus() = CurrentStatus(
  statusDescriptionId = referralStatusDescription.id,
  title = referralStatusDescription.description,
  tagColour = referralStatusDescription.labelColour,
  updatedByName = createdBy,
  createdAt = createdAt.toLocalDate(),
)
