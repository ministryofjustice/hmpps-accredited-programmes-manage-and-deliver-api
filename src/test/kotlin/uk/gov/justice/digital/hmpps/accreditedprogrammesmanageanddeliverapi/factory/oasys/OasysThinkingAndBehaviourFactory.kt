package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysThinkingAndBehaviour

class OasysThinkingAndBehaviourFactory {
  private var temperControl: String? = "1 - Some problems"
  private var problemSolvingSkills: String? = "2 - Significant problems"
  private var awarenessOfConsequences: String? = "0 - No problems"
  private var understandsViewsOfOthers: String? = "0 - No problems"
  private var achieveGoals: String? = "0 - No problems"
  private var concreteAbstractThinking: String? = "0 - No problems"

  fun withTemperControl(temperControl: String?) = apply {
    this.temperControl = temperControl
  }

  fun withProblemSolvingSkills(problemSolvingSkills: String?) = apply {
    this.problemSolvingSkills = problemSolvingSkills
  }

  fun withAwarenessOfConsequences(awarenessOfConsequences: String?) = apply {
    this.awarenessOfConsequences = awarenessOfConsequences
  }

  fun withUnderstandsViewsOfOthers(understandsViewsOfOthers: String?) = apply {
    this.understandsViewsOfOthers = understandsViewsOfOthers
  }

  fun withAchieveGoals(achieveGoals: String?) = apply {
    this.achieveGoals = achieveGoals
  }

  fun withConcreteAbstractThinking(concreteAbstractThinking: String?) = apply {
    this.concreteAbstractThinking = concreteAbstractThinking
  }

  fun produce() = OasysThinkingAndBehaviour(
    temperControl = this.temperControl,
    problemSolvingSkills = this.problemSolvingSkills,
    awarenessOfConsequences = this.awarenessOfConsequences,
    understandsViewsOfOthers = this.understandsViewsOfOthers,
    achieveGoals = this.achieveGoals,
    concreteAbstractThinking = this.concreteAbstractThinking,
  )
}
