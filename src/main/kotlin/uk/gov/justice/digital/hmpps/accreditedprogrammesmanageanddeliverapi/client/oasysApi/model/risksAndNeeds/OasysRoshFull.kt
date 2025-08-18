package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshFull(
  val currentOffenceDetails: String?,
  val currentWhereAndWhen: String?,
  val currentHowDone: String?,
  val currentWhoVictims: String?,
  val currentAnyoneElsePresent: String?,
  val currentWhyDone: String?,
  val currentSources: String?,
  val identifyBehavioursIncidents: String?,
  val analysisBehavioursIncidents: String?,
)

fun OasysRoshFull.toModel(assessmentCompletedDate: LocalDateTime?): RoshAnalysis = RoshAnalysis(
  assessmentCompleted = assessmentCompletedDate?.toLocalDate(),
  offenceDetails = currentOffenceDetails,
  whereAndWhen = currentWhereAndWhen,
  howDone = currentHowDone,
  whoVictims = currentWhoVictims,
  anyoneElsePresent = currentAnyoneElsePresent,
  whyDone = currentWhyDone,
  sources = currentSources,
  identifyBehavioursIncidents = identifyBehavioursIncidents,
  analysisBehaviourIncidents = analysisBehavioursIncidents,
)
