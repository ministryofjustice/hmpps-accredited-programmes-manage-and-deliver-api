package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
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
    example = "X933590",
    required = true,
    description = "The crn associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

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
    example = "11 June 2023",
    required = true,
    description = "Timestamp of when this referral was created.",
  )
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDate,

  @Schema(
    example = "15 March 1985",
    required = true,
    description = "The date of birth of the person being referred.",
  )
  @get:JsonProperty("dateOfBirth", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val dateOfBirth: LocalDate,

  @Schema(
    example = "Tom Saunders",
    required = true,
    description = "The name of the probation practitioner associated with this referral.",
  )
  @get:JsonProperty("probationPractitionerName", required = true)
  val probationPractitionerName: String?,

  @Schema(
    example = "tom.saunders@justice.gov.uk",
    required = true,
    description = "The email of the probation practitioner associated with this referral.",
  )
  @get:JsonProperty("probationPractitionerEmail", required = false)
  val probationPractitionerEmail: String? = null,

  @Schema(
    example = "SEXUAL_OFFENCE",
    required = true,
    description = "The offence cohort this referral is classified as.",
  )
  @get:JsonProperty("cohort", required = true)
  val cohort: OffenceCohort,

  ) {
  companion object {
    fun toModel(referral: ReferralEntity, nDeliusPersonalDetails: NDeliusPersonalDetails): ReferralDetails = ReferralDetails(
      id = referral.id!!,
      crn = referral.crn,
      personName = nDeliusPersonalDetails.name.getNameAsString(),
      interventionName = referral.interventionName ?: "UNKNOWN_INTERVENTION",
      createdAt = referral.createdAt.toLocalDate(),
      dateOfBirth = LocalDate.parse(nDeliusPersonalDetails.dateOfBirth),
      probationPractitionerName = nDeliusPersonalDetails.probationPractitioner?.name?.getNameAsString(),
      probationPractitionerEmail = nDeliusPersonalDetails.probationPractitioner?.email,
      cohort = referral.cohort ?: OffenceCohort.GENERAL_OFFENCE,
      )
  }
}
