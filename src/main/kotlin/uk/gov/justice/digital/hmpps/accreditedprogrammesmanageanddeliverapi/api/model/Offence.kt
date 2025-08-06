package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Details of an offence committed by an offender")
data class Offence(
  @Schema(description = "The date when the offence was committed", example = "2024-01-15")
  val offenceDate: LocalDate,
  @Schema(description = "The description of the offence", example = "Theft")
  val offence: String,
  @Schema(description = "The code identifying the offence", example = "68")
  val offenceCode: String,
  @Schema(description = "The category of the offence", example = "Theft and burglary offences")
  val category: String,
  @Schema(description = "The code identifying the offence category", example = "12")
  val categoryCode: String,
)
