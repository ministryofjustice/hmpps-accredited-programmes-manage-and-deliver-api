package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offence as NDeliusOffence

@Schema(description = "Represents an individual's history of offences, including their main offence and any additional offences")
data class OffenceHistory(
  @Schema(description = "The primary offence committed")
  val mainOffence: Offence,
  @Schema(description = "List of additional or secondary offences")
  val additionalOffences: List<Offence> = emptyList(),
  @Schema(description = "The date the offence history was imported from NDelius", example = "11 June 2020")
  @JsonFormat(pattern = "d MMMM yyyy")
  val importedDate: LocalDate = LocalDate.now(),
)

fun Offences.toApi() = OffenceHistory(
  mainOffence = mainOffence.toApi(),
  additionalOffences = additionalOffences.map { it.toApi() },
)

fun NDeliusOffence.toApi() = Offence(
  offenceDate = date,
  offence = mainCategoryDescription,
  offenceCode = mainCategoryCode,
  category = subCategoryDescription,
  categoryCode = subCategoryCode,
)
