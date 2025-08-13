package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Behaviour

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysBehaviour(
  val temperControl: String?,
  val problemSolvingSkills: String?,
  val awarenessOfConsequences: String?,
  val achieveGoals: String?,
  val understandsViewsOfOthers: String?,
  val concreteAbstractThinking: String?,
  val sexualPreOccupation: String?,
  val offenceRelatedSexualInterests: String?,
  // TODO CHECK SPELLING HERE
  val aggressiveControllingBehavour: String?,
  val impulsivity: String?,
)

fun OasysBehaviour.toModel(): Behaviour = Behaviour(
  temperControl = temperControl,
  problemSolvingSkills = problemSolvingSkills,
  awarenessOfConsequences = awarenessOfConsequences,
  achieveGoals = achieveGoals,
  understandsViewsOfOthers = understandsViewsOfOthers,
  concreteAbstractThinking = concreteAbstractThinking,
  sexualPreOccupation = sexualPreOccupation,
  offenceRelatedSexualInterests = offenceRelatedSexualInterests,
  aggressiveControllingBehaviour = aggressiveControllingBehavour,
  impulsivity = impulsivity,
)
