package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SentenceInformation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import java.time.LocalDate

class SentenceInformationFactory {
  private var sentenceType: String? = randomSentence(wordRange = 2..4)
  private var releaseType: String? = "Released on licence"
  private var licenceConditions: List<CodeDescription>? = listOf(
    createCodeDescription(),
    createCodeDescription(),
  )
  private var licenceEndDate: LocalDate? = LocalDate.now().plusYears(2)
  private var postSentenceSupervisionStartDate: LocalDate? = LocalDate.now().plusMonths(18)
  private var postSentenceSupervisionEndDate: LocalDate? = LocalDate.now().plusYears(3)
  private var twoThirdsPoint: LocalDate? = LocalDate.now().plusMonths(20)
  private var orderRequirements: List<CodeDescription>? = listOf(
    createCodeDescription(),
    createCodeDescription(),
  )
  private var orderEndDate: LocalDate? = LocalDate.now().plusYears(1)
  private var dateRetrieved: LocalDate = LocalDate.now()

  fun createCodeDescription(): CodeDescription = CodeDescription(randomUppercaseString(2), randomSentence())
  fun withSentenceType(sentenceType: String?) = apply { this.sentenceType = sentenceType }
  fun withReleaseType(releaseType: String?) = apply { this.releaseType = releaseType }
  fun withLicenceConditions(licenceConditions: List<CodeDescription>?) = apply { this.licenceConditions = licenceConditions }

  fun withLicenceEndDate(licenceEndDate: LocalDate?) = apply { this.licenceEndDate = licenceEndDate }
  fun withPostSentenceSupervisionStartDate(postSentenceSupervisionStartDate: LocalDate?) = apply { this.postSentenceSupervisionStartDate = postSentenceSupervisionStartDate }

  fun withPostSentenceSupervisionEndDate(postSentenceSupervisionEndDate: LocalDate?) = apply { this.postSentenceSupervisionEndDate = postSentenceSupervisionEndDate }

  fun withTwoThirdsPoint(twoThirdsPoint: LocalDate?) = apply { this.twoThirdsPoint = twoThirdsPoint }
  fun withOrderRequirements(orderRequirements: List<CodeDescription>?) = apply { this.orderRequirements = orderRequirements }

  fun withOrderEndDate(orderEndDate: LocalDate?) = apply { this.orderEndDate = orderEndDate }
  fun withDateRetrieved(dateRetrieved: LocalDate) = apply { this.dateRetrieved = dateRetrieved }

  fun produce() = SentenceInformation(
    sentenceType = this.sentenceType,
    releaseType = this.releaseType,
    licenceConditions = this.licenceConditions,
    licenceEndDate = this.licenceEndDate,
    postSentenceSupervisionStartDate = this.postSentenceSupervisionStartDate,
    postSentenceSupervisionEndDate = this.postSentenceSupervisionEndDate,
    twoThirdsPoint = this.twoThirdsPoint,
    orderRequirements = this.orderRequirements,
    orderEndDate = this.orderEndDate,
    dateRetrieved = this.dateRetrieved,
  )
}
