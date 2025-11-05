package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ReferralMotivationBackgroundAndNonAssociations(
  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of the ReferralMotivationBackgroundAndNonAssociations information.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("referralId", required = true)
  val referralId: UUID,

  @Schema(
    example = "true",
    required = true,
    description = "Boolean value indicating whether the referral maintains innocence.",
  )
  @get:JsonProperty("maintainsInnocence", required = true)
  val maintainsInnocence: Boolean,

  @Schema(
    example = "Motivated to change and improve life circumstances.",
    required = true,
    description = "Information on the motivation to participate in an accredited programme.",
  )
  @get:JsonProperty("motivations", required = true)
  val motivations: String,

  @Schema(
    example = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
    required = true,
    description = "Information on any non-associations relevant to the referral.",
  )
  @get:JsonProperty("otherConsiderations", required = true)
  val otherConsiderations: String,

  @Schema(
    example = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
    required = true,
    description = "Information on any non-associations relevant to the referral.",
  )
  @get:JsonProperty("nonAssociations", required = true)
  val nonAssociations: String,

  @Schema(
    example = "11 June 2023",
    required = true,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDate,

  @get:JsonProperty("createdBy")
  @Schema(description = "The user that last created the delivery location preferences")
  val createdBy: String? = null,

  @Schema(
    example = "11 June 2023",
    required = true,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("lastUpdatedAt")
  val lastUpdatedAt: LocalDateTime?,

  @get:JsonProperty("lastUpdatedBy")
  @Schema(description = "The user that last created the delivery location preferences")
  val lastUpdatedBy: String? = null,
) {
  companion object {
    fun toApi(
      referralMotivationBackgroundAndNonAssociations: ReferralMotivationBackgroundAndNonAssociationsEntity,
    ): ReferralMotivationBackgroundAndNonAssociations = ReferralMotivationBackgroundAndNonAssociations(
      id = referralMotivationBackgroundAndNonAssociations.id!!,
      referralId = referralMotivationBackgroundAndNonAssociations.referral.id!!,
      maintainsInnocence = referralMotivationBackgroundAndNonAssociations.maintainsInnocence,
      motivations = referralMotivationBackgroundAndNonAssociations.motivations,
      nonAssociations = referralMotivationBackgroundAndNonAssociations.nonAssociations,
      otherConsiderations = referralMotivationBackgroundAndNonAssociations.otherConsiderations,
      createdAt = referralMotivationBackgroundAndNonAssociations.createdAt.toLocalDate(),
      createdBy = referralMotivationBackgroundAndNonAssociations.createdBy,
      lastUpdatedAt = referralMotivationBackgroundAndNonAssociations.lastUpdatedAt,
      lastUpdatedBy = referralMotivationBackgroundAndNonAssociations.lastUpdatedBy,
    )
  }
}
