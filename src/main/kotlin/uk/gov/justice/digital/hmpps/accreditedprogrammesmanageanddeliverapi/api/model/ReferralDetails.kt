package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.LdcStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.mostRecentStatus
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

  @Schema(
    example = "True",
    required = true,
    description = "Does the person this referral is associated with have LDC needs",
  )
  @get:JsonProperty("hasLdc", required = true)
  val hasLdc: Boolean = LdcStatus.NO_LDC.value,

  @Schema(
    example = "May need an LDC-adapted programme(Building Choices Plus)",
    required = true,
    description = "The text to display in the UI for the LDC status of this referral",
  )
  @get:JsonProperty("hasLdcDisplayText", required = true)
  val hasLdcDisplayText: String = LdcStatus.NO_LDC.displayText,

  @Schema(
    example = "Awaiting assessment",
    required = true,
    description = "The display name of the Referral's current Status",
  )
  @get:JsonProperty("currentStatusDescription", required = true)
  val currentStatusDescription: String,

) {
  companion object {
    fun toModel(
      referral: ReferralEntity,
      nDeliusPersonalDetails: NDeliusPersonalDetails,
      hasLdc: Boolean? = false,
    ): ReferralDetails = ReferralDetails(
      id = referral.id!!,
      crn = referral.crn,
      personName = nDeliusPersonalDetails.name.getNameAsString(),
      interventionName = referral.interventionName ?: "UNKNOWN_INTERVENTION",
      createdAt = referral.createdAt.toLocalDate(),
      dateOfBirth = LocalDate.parse(nDeliusPersonalDetails.dateOfBirth),
      probationPractitionerName = nDeliusPersonalDetails.probationPractitioner?.name?.getNameAsString(),
      probationPractitionerEmail = nDeliusPersonalDetails.probationPractitioner?.email,
      cohort = referral.cohort,
      hasLdc = LdcStatus.fromBoolean(hasLdc).value,
      hasLdcDisplayText = LdcStatus.getDisplayText(hasLdc),
      currentStatusDescription = referral.mostRecentStatus().description,
    )
  }
}
