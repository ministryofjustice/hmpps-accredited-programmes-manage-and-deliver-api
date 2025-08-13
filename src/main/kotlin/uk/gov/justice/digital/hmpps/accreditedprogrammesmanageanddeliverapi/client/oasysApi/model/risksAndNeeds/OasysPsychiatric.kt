package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Psychiatric

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysPsychiatric(
  val currPsychiatricProblems: String?,
  val difficultiesCoping: String?,
  val currPsychologicalProblems: String?,
  val selfHarmSuicidal: String?,
)

fun OasysPsychiatric.toModel() = Psychiatric(
  description = currPsychiatricProblems,
  difficultiesCoping = difficultiesCoping,
  currPsychologicalProblems = currPsychologicalProblems,
  selfHarmSuicidal = selfHarmSuicidal,
)
