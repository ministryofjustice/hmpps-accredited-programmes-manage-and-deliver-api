package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshFull(
  val currentOffenceDetails: String?,
  val currentWhereAndWhen: String?,
  val currentHowDone: String?,
  val currentWhoVictims: String?,
  val currentAnyoneElsePresent: String?,
  val currentWhyDone: String?,
  val currentSources: String?,
)

fun OasysRoshFull.toModel() = RoshAnalysis(
  offenceDetails = currentOffenceDetails,
  whereAndWhen = currentWhereAndWhen,
  howDone = currentHowDone,
  whoVictims = currentWhoVictims,
  anyoneElsePresent = currentAnyoneElsePresent,
  whyDone = currentWhyDone,
  sources = currentSources,
)
