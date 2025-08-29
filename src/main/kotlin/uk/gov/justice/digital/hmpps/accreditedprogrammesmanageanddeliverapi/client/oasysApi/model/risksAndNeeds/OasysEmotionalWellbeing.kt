package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.EmotionalWellbeing
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysEmotionalWellbeing(
  val currPsychologicalProblems: String? = null,
  val selfHarmSuicidal: String? = null,
  val currPsychiatricProblems: String? = null,
)

fun OasysEmotionalWellbeing.toModel(assessmentCompletedDate: LocalDate?) = EmotionalWellbeing(
  assessmentCompleted = assessmentCompletedDate,
  currentPsychologicalProblems = currPsychologicalProblems,
  selfHarmSuicidal = selfHarmSuicidal,
  currentPsychiatricProblems = currPsychiatricProblems,
)
