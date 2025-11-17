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
    required = false,
    description = "The unique id of the ReferralMotivationBackgroundAndNonAssociations information.",
  )
  @get:JsonProperty("id", required = false)
  val id: UUID? = null,

  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = false,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("referralId", required = false)
  val referralId: UUID? = null,

  @Schema(
    example = "true",
    required = false,
    description = "Boolean value indicating whether the referral maintains innocence.",
  )
  @get:JsonProperty("maintainsInnocence", required = false)
  val maintainsInnocence: Boolean? = null,

  @Schema(
    example = "Motivated to change and improve life circumstances.",
    required = false,
    description = "Information on the motivation to participate in an accredited programme.",
  )
  @get:JsonProperty("motivations", required = false)
  val motivations: String? = null,

  @Schema(
    example = "Other information relevant to the referral.",
    required = true,
    description = "Any other relevant information that should be considered.",
  )
  @get:JsonProperty("otherConsiderations", required = true)
  val otherConsiderations: String?,

  @Schema(
    example = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
    required = false,
    description = "Information on any non-associations relevant to the referral.",
  )
  @get:JsonProperty("nonAssociations", required = false)
  val nonAssociations: String? = null,

  @Schema(
    example = "11 June 2023",
    required = false,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("createdAt", required = false)
  val createdAt: LocalDate? = null,

  @get:JsonProperty("createdBy")
  @Schema(description = "The user that last created the delivery location preferences")
  val createdBy: String? = null,

  @Schema(
    example = "11 June 2023",
    required = false,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("lastUpdatedAt")
  val lastUpdatedAt: LocalDateTime? = null,

  @get:JsonProperty("lastUpdatedBy")
  @Schema(description = "The user that last created the delivery location preferences")
  val lastUpdatedBy: String? = null,
) {
  companion object {
    fun toApi(
      referralMotivationBackgroundAndNonAssociations: ReferralMotivationBackgroundAndNonAssociationsEntity?,
    ): ReferralMotivationBackgroundAndNonAssociations = ReferralMotivationBackgroundAndNonAssociations(
      id = referralMotivationBackgroundAndNonAssociations?.id,
      referralId = referralMotivationBackgroundAndNonAssociations?.referral?.id,
      maintainsInnocence = referralMotivationBackgroundAndNonAssociations?.maintainsInnocence,
      motivations = referralMotivationBackgroundAndNonAssociations?.motivations,
      nonAssociations = referralMotivationBackgroundAndNonAssociations?.nonAssociations,
      otherConsiderations = referralMotivationBackgroundAndNonAssociations?.otherConsiderations,
      createdAt = referralMotivationBackgroundAndNonAssociations?.createdAt?.toLocalDate(),
      createdBy = referralMotivationBackgroundAndNonAssociations?.createdBy,
      lastUpdatedAt = referralMotivationBackgroundAndNonAssociations?.lastUpdatedAt,
      lastUpdatedBy = referralMotivationBackgroundAndNonAssociations?.lastUpdatedBy,
    )
  }
}
