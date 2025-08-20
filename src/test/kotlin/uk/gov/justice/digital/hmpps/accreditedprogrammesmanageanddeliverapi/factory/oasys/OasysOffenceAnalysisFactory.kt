package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import kotlin.random.Random

class OasysOffenceAnalysisFactory {
  private var offenceAnalysis: String? =
    "Ms Puckett admits he went to Mr X 's address on 23rd march 2010. He went there in order to buy cannabis. This meeting had been pre- arranged and Ms Puckett went there with another man. Mr X sold an ounce of cannabis for #120 to Ms Puckett.  Together, Ms Puckett and the other man queried the quality of the ounce of cannabis. Ms Puckett asked for her money back.  Mr X told them there was nothing wrong with the cannabis and asked them both to leave. Ms Puckett grabbed Mr X and said she wanted her money back.  Mr X said something.  Ms Puckett then punched him once with her fist. Mr X then attacked Ms Puckett with an implement from the side or the back of the sofa. He struck Ms Puckett to the back of the head. In the course of the struggle Ms Puckett accepts that he came into possession of the implement with shich Mr X had attacked her. During the course of the struggle Ms Puckett accepts that she struck Mr X with the implement 2 or 3 times. As a result she is guilty of unlawful wounding."
  private var whatOccurred: List<String>? =
    OasysOffenceAnalysis.WhatOccurred.entries.shuffled().take(Random.nextInt(1, 4)).map { it.description }
  private var recognisesImpact: String? = listOf("YES", "NO").random()
  private var numberOfOthersInvolved: String? = randomNumber(1).toString()
  private var othersInvolved: String? = "YES"
  private var peerGroupInfluences: String? = "YES"
  private var offenceMotivation: String? =
    "Ms Puckett stated that as she had been attempting to address his long standing addiction to heroin, with the support of a Drug Rehabilitation Requirement as part of a community order, he had been using cannabis as a substitute in order to assuage symptoms of withdrawal or stress."
  private var acceptsResponsibilityYesNo: String? = listOf("YES", "NO").random()
  private var acceptsResponsibility: String? = randomSentence(wordRange = 4..12)
  private var patternOffending: String? =
    "Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention."

  fun withOffenceAnalysis(offenceAnalysis: String?) = apply { this.offenceAnalysis = offenceAnalysis }
  fun withWhatOccurred(whatOccurred: List<String>?) = apply { this.whatOccurred = whatOccurred }
  fun withRecognisesImpact(recognisesImpact: String?) = apply { this.recognisesImpact = recognisesImpact }
  fun withNumberOfOthersInvolved(numberOfOthersInvolved: String?) = apply { this.numberOfOthersInvolved = numberOfOthersInvolved }

  fun withOthersInvolved(othersInvolved: String?) = apply { this.othersInvolved = othersInvolved }
  fun withPeerGroupInfluences(peerGroupInfluences: String?) = apply { this.peerGroupInfluences = peerGroupInfluences }
  fun withOffenceMotivation(offenceMotivation: String?) = apply { this.offenceMotivation = offenceMotivation }
  fun withAcceptsResponsibilityYesNo(acceptsResponsibilityYesNo: String?) = apply { this.acceptsResponsibilityYesNo = acceptsResponsibilityYesNo }

  fun withAcceptsResponsibility(acceptsResponsibility: String?) = apply { this.acceptsResponsibility = acceptsResponsibility }

  fun withPatternOffending(patternOffending: String?) = apply { this.patternOffending = patternOffending }

  fun produce() = OasysOffenceAnalysis(
    offenceAnalysis = this.offenceAnalysis,
    whatOccurred = this.whatOccurred,
    recognisesImpact = this.recognisesImpact,
    numberOfOthersInvolved = this.numberOfOthersInvolved,
    othersInvolved = this.othersInvolved,
    peerGroupInfluences = this.peerGroupInfluences,
    offenceMotivation = this.offenceMotivation,
    acceptsResponsibilityYesNo = this.acceptsResponsibilityYesNo,
    acceptsResponsibility = this.acceptsResponsibility,
    patternOffending = this.patternOffending,
  )
}
