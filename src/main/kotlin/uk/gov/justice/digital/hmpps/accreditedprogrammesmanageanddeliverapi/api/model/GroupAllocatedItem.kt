package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class GroupAllocatedItem(
  @Schema(
    example = "1ff57cea-352c-4a99-8f66-3e626aac3265",
    required = true,
    description = "The UUID of the referral.",
  )
  @get:JsonProperty("referralId", required = true)
  val referralId: UUID,
  @Schema(
    example = "Requirement",
    required = true,
    description = "The entity (Licence Condition or Requirement) that caused the Referral to be created in our system",
  )
  @get:JsonProperty("sourcedFrom", required = true)
  val sourcedFrom: String? = null,
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
    example = "1 January 2030",
    required = true,
    description = "The end date of the person's sentence",
  )
  @get:JsonProperty("sentenceEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val sentenceEndDate: LocalDate?,

  @Schema(
    example = "Awaiting allocation",
    required = true,
    description = "The status of the referral",
  )
  @get:JsonProperty("status", required = true)
  val status: String,
)
