package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offence as NDeliusOffence

@Schema(description = "Represents a individuals history of offences, including their main offence and any additional offences")
data class OffenceHistory(
  @Schema(description = "The primary offence committed")
  val mainOffence: Offence,
  @Schema(description = "List of additional or secondary offences")
  val additionalOffences: List<Offence>,
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
