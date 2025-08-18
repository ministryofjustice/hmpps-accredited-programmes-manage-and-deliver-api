package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshFull

class OasysRoshFullFactory {

  private var currentOffenceDetails: String? =
    "Ms Puckett admits he went to Mr X's address on 23rd march 2010. She went there in order to buy cannabis."
  private var currentWhereAndWhen: String? = "At the victim's home address, in the evening"
  private var currentHowDone: String? =
    "Appears to have been unprovoked violence - although basis of plea indicates otherwise. In any event this was impulsive, excessive violence using a weapon (metal pole)"
  private var currentWhoVictims: String? = "Male-outnumbered by Mr Manette and his associate (not charged)"
  private var currentAnyoneElsePresent: String? =
    "See above - Mr Manette was in the company of another, although he was not apprehended or charged for this offence."
  private var currentWhyDone: String? =
    "Ms Puckett stated that as she had been attempting to address her long standing addiction to heroin."
  private var currentSources: String? = "Interview, CPS documentation, basis of plea."
  private var identifyBehavioursIncidents: String? =
    "Physical assault on cellmate requiring medical attention on 22nd March 2024. Weapon possession (improvised blade) discovered during cell search on 8th February 2024."
  private var analysisBehavioursIncidents: String? =
    "Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention."

  fun withCurrentOffenceDetails(currentOffenceDetails: String?) = apply { this.currentOffenceDetails = currentOffenceDetails }

  fun withCurrentWhereAndWhen(currentWhereAndWhen: String?) = apply { this.currentWhereAndWhen = currentWhereAndWhen }
  fun withCurrentHowDone(currentHowDone: String?) = apply { this.currentHowDone = currentHowDone }
  fun withCurrentWhoVictims(currentWhoVictims: String?) = apply { this.currentWhoVictims = currentWhoVictims }
  fun withCurrentAnyoneElsePresent(currentAnyoneElsePresent: String?) = apply { this.currentAnyoneElsePresent = currentAnyoneElsePresent }

  fun withCurrentWhyDone(currentWhyDone: String?) = apply { this.currentWhyDone = currentWhyDone }
  fun withCurrentSources(currentSources: String?) = apply { this.currentSources = currentSources }

  fun produce() = OasysRoshFull(
    currentOffenceDetails = this.currentOffenceDetails,
    currentWhereAndWhen = this.currentWhereAndWhen,
    currentHowDone = this.currentHowDone,
    currentWhoVictims = this.currentWhoVictims,
    currentAnyoneElsePresent = this.currentAnyoneElsePresent,
    currentWhyDone = this.currentWhyDone,
    currentSources = this.currentSources,
    identifyBehavioursIncidents = identifyBehavioursIncidents,
    analysisBehavioursIncidents = analysisBehavioursIncidents,
  )
}
