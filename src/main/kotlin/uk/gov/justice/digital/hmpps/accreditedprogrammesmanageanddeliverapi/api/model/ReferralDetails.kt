package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDate
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
    example = "52",
    required = true,
    description = "The age of the person asscociated with this referral.",
  )
  val age: String,

  @Schema(
    example = "X12345",
    required = true,
    description = "The CRN identifier of the person associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @Schema(
    example = "1981-04-15",
    required = true,
    description = "The date of birth of the person associated with this referral.",
  )
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate,

  @Schema(
    example = "White",
    required = true,
    description = "The ethnicity of the person associated with this referral.",
  )
  val ethnicity: String,

  @Schema(
    example = "Male",
    required = true,
    description = "The gender of the person associated with this referral.",
  )
  val gender: String,

  @Schema(
    example = "John Doe",
    required = true,
    description = "The name of the person associated with this referral.",
  )
  @get:JsonProperty("personName", required = true)
  val personName: String,

  @Schema(
    example = "Brighton and East Sussex",
    required = true,
    description = "The Probation Delivery Unit of the person associated with this referral.",
  )
  val probationDeliveryUnit: String,

  @Schema(
    example = "COMMUNITY",
    required = true,
    description = "The setting the referral is associated with.",
  )
  val setting: String,

) {
  companion object {
    fun toModel(referral: ReferralEntity, personalDetails: ServiceUser): ReferralDetails = ReferralDetails(
      id = referral.id!!,
      crn = referral.crn,
      personName = personalDetails.name,
      dateOfBirth = personalDetails.dateOfBirth,
      age = personalDetails.age,
      ethnicity = personalDetails.ethnicity,
      gender = personalDetails.gender,
      setting = referral.setting,
      probationDeliveryUnit = personalDetails.currentPdu,
    )
  }
}
