package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Attitude(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted")
  val assessmentCompleted: LocalDate? = null,

  @Schema(example = "2 - Significant problems")
  @get:JsonProperty("proCriminalAttitudes")
  val proCriminalAttitudes: String? = null,

  @Schema(example = "1 - Some motivation to change")
  @get:JsonProperty("motivationToAddressBehaviour")
  val motivationToAddressBehaviour: String? = null,

  @Schema(example = "0 - No hostile orientation")
  @get:JsonProperty("hostileOrientation")
  val hostileOrientation: String? = null,
)
