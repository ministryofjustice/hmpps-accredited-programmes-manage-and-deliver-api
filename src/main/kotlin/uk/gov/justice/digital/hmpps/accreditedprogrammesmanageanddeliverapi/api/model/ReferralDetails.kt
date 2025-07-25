package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

data class ReferralDetails(
  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "John Doe",
    required = true,
    description = "The name of the person associated with this referral.",
  )
  @get:JsonProperty("personName", required = true)
  val personName: String,

  @Schema(
    example = "Building Choices",
    required = true,
    description = "The name of the Intervention for this referral.",
  )
  @get:JsonProperty("interventionName", required = true)
  val interventionName: String,

  @Schema(
    example = "2025-06-14 12:56:23",
    required = true,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDateTime,

  @Schema(
    example = "Tom Saunders",
    required = true,
    description = "The name of the probation practitioner associated with this referral.",
  )
  @get:JsonProperty("probationPractitionerName", required = true)
  val probationPractitionerName: String,

  @Schema(
    example = "tom.saunders@justice.gov.uk",
    required = true,
    description = "The email of the probation practitioner associated with this referral.",
  )
  @get:JsonProperty("probationPractitionerEmail", required = true)
  val probationPractitionerEmail: String,

) {
  companion object {
    fun toModel(referral: ReferralEntity, NDeliusPersonalDetails: NDeliusPersonalDetails): ReferralDetails = ReferralDetails(
      id = referral.id!!,
      personName = NDeliusPersonalDetails.name.getNameAsString(),
      interventionName = referral.interventionName ?: "UNKNOWN_INTERVENTION",
      createdAt = referral.createdAt,
      probationPractitionerName = NDeliusPersonalDetails.probationPractitioner.name.getNameAsString(),
      probationPractitionerEmail = NDeliusPersonalDetails.probationPractitioner.email,
    )
  }
}
