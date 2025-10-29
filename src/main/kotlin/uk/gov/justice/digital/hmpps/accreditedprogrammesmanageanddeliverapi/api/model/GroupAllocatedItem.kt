package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class GroupAllocatedItem(
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
    example = "15 March 1985",
    required = true,
    description = "The end date of the person's sentence",
  )
  @get:JsonProperty("sentenceEndDate", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val sentenceEndDate: LocalDate,
  @Schema(
    example = "Awaiting allocation",
    required = true,
    description = "The status of the referral",
  )
  @get:JsonProperty("status", required = true)
  val status: String,
)
