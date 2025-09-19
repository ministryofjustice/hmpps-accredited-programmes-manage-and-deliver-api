package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.util.UUID

data class ReferralStatus(
  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral status.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "Awaiting assessment",
    required = true,
    description = "The status description text.",
  )
  @get:JsonProperty("status", required = true)
  val status: String,

  @Schema(
    example = "The person has completed the programme. The referral will be closed.",
    required = true,
    description = "The description text for this particular status transition",
  )
  @get:JsonProperty("transitionDescription", required = true)
  val transitionDescription: String,

  @Schema(
    example = "false",
    required = true,
    description = "Whether this status represents a closed status for the referral.",
  )
  @get:JsonProperty("isClosed", required = true)
  val isClosed: Boolean,

  @Schema(
    example = "orange",
    required = false,
    description = "The color to be used for displaying this status label.",
  )
  @get:JsonProperty("labelColour", required = false)
  val labelColour: String?,
)

fun ReferralStatusDescriptionEntity.toApi(transitionDescription: String) = ReferralStatus(
  id = id,
  status = description,
  isClosed = isClosed,
  labelColour = labelColour,
  transitionDescription = transitionDescription,
)
