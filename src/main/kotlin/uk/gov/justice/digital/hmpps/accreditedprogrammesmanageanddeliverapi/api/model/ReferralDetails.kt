package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.PersonalDetails
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

  val interventionName: String,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val createdAt: LocalDateTime,
  val probationPractitionerName: String,
  val probationPractitionerEmail: String,

) {
  companion object {
    fun toModel(referral: ReferralEntity, personalDetails: PersonalDetails): ReferralDetails = ReferralDetails(
      id = referral.id!!,
      personName = personalDetails.name.getNameAsString(),
      interventionName = referral.interventionName ?: "UNKNOWN_INTERVENTION",
      createdAt = referral.createdAt,
      probationPractitionerName = personalDetails.probationPractitioner.name.getNameAsString(),
      probationPractitionerEmail = personalDetails.probationPractitioner.email,
    )
  }
}
