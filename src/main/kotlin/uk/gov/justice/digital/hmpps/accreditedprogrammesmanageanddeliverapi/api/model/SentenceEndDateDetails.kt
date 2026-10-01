package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class SentenceEndDateDetails(
  @Schema(
    example = "1 January 2030",
    required = false,
    description = "The expected end date of the sentence.",
  )
  @get:JsonProperty("expectedEndDate", required = false)
  @get:JsonFormat(pattern = "d MMMM yyyy")
  val expectedEndDate: LocalDate? = null,

  @Schema(
    example = "1 January 2030",
    required = false,
    description = "The licence expiry date. Populated for licence cases and null for requirement cases.",
  )
  @get:JsonProperty("licenceExpiryDate", required = false)
  @get:JsonFormat(pattern = "d MMMM yyyy")
  val licenceExpiryDate: LocalDate? = null,
)
