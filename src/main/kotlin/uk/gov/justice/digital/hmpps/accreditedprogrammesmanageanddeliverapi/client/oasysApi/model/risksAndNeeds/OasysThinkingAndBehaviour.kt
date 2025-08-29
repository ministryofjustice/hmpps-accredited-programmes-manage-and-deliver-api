package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.ThinkingAndBehaviour
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysThinkingAndBehaviour(
  val temperControl: String? = null,
  val problemSolvingSkills: String? = null,
  val awarenessOfConsequences: String? = null,
  val understandsViewsOfOthers: String? = null,
  val achieveGoals: String? = null,
  val concreteAbstractThinking: String? = null,
)

fun OasysThinkingAndBehaviour.toModel(assessmentCompletedDate: LocalDate?) = ThinkingAndBehaviour(
  assessmentCompleted = assessmentCompletedDate,
  temperControl = temperControl,
  problemSolvingSkills = problemSolvingSkills,
  awarenessOfConsequences = awarenessOfConsequences,
  understandsViewsOfOthers = understandsViewsOfOthers,
  achieveGoals = achieveGoals,
  concreteAbstractThinking = concreteAbstractThinking,
)
