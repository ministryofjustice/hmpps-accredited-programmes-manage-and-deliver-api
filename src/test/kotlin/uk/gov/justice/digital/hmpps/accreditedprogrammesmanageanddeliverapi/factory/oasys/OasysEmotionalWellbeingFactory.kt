package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysEmotionalWellbeing

class OasysEmotionalWellbeingFactory {
  private var currPsychologicalProblems: String? = "1 - Some problems"
  private var selfHarmSuicidal: String? = "0 - No"
  private var currPsychiatricProblems: String? = "0 – No problems"

  fun withCurrPsychologicalProblems(currPsychologicalProblems: String?) = apply {
    this.currPsychologicalProblems = currPsychologicalProblems
  }

  fun withSelfHarmSuicidal(selfHarmSuicidal: String?) = apply {
    this.selfHarmSuicidal = selfHarmSuicidal
  }

  fun withCurrPsychiatricProblems(currPsychiatricProblems: String?) = apply { this.currPsychiatricProblems = currPsychiatricProblems }

  fun produce() = OasysEmotionalWellbeing(
    currPsychologicalProblems = this.currPsychologicalProblems,
    selfHarmSuicidal = this.selfHarmSuicidal,
    currPsychiatricProblems = this.currPsychiatricProblems,
  )
}
