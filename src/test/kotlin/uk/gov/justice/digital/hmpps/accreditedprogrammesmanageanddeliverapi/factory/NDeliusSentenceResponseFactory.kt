package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescriptionList
import java.time.LocalDate

class NDeliusSentenceResponseFactory {
  private var description: String? = randomSentence(wordRange = 2..5)
  private var startDate: LocalDate = LocalDate.now().minusYears(1)
  private var expectedEndDate: LocalDate = LocalDate.now()
  private var licenceExpiryDate: LocalDate? = LocalDate.now().plusYears(2)
  private var postSentenceSupervisionEndDate: LocalDate? = LocalDate.now().plusYears(3)
  private var twoThirdsSupervisionDate: LocalDate? = LocalDate.now().plusMonths(18)
  private var custodial: Boolean = true
  private var releaseType: String? = "Standard Release"

  private var licenceConditions: List<CodeDescription> = listOf(createCodeDescription(), createCodeDescription())
  private var requirements: List<CodeDescription> = createCodeDescriptionList()
  private var postSentenceSupervisionRequirements: List<CodeDescription> = createCodeDescriptionList()

  fun withDescription(description: String?) = apply { this.description = description }
  fun withStartDate(startDate: LocalDate) = apply { this.startDate = startDate }
  fun withExpectedEndDate(expectedEndDate: LocalDate) = apply { this.expectedEndDate = startDate }
  fun withLicenceExpiryDate(licenceExpiryDate: LocalDate?) = apply { this.licenceExpiryDate = licenceExpiryDate }
  fun withPostSentenceSupervisionEndDate(postSentenceSupervisionEndDate: LocalDate?) = apply { this.postSentenceSupervisionEndDate = postSentenceSupervisionEndDate }

  fun withTwoThirdsSupervisionDate(twoThirdsSupervisionDate: LocalDate?) = apply { this.twoThirdsSupervisionDate = twoThirdsSupervisionDate }

  fun withCustodial(custodial: Boolean) = apply { this.custodial = custodial }
  fun withReleaseType(releaseType: String?) = apply { this.releaseType = releaseType }
  fun withLicenceConditions(licenceConditions: List<CodeDescription>) = apply { this.licenceConditions = licenceConditions }

  fun withRequirements(requirements: List<CodeDescription>) = apply { this.requirements = requirements }
  fun withPostSentenceSupervisionRequirements(postSentenceSupervisionRequirements: List<CodeDescription>) = apply { this.postSentenceSupervisionRequirements = postSentenceSupervisionRequirements }

  fun produce() = NDeliusSentenceResponse(
    description = this.description,
    startDate = this.startDate,
    licenceExpiryDate = this.licenceExpiryDate,
    postSentenceSupervisionEndDate = this.postSentenceSupervisionEndDate,
    twoThirdsSupervisionDate = this.twoThirdsSupervisionDate,
    custodial = this.custodial,
    releaseType = this.releaseType,
    licenceConditions = this.licenceConditions,
    requirements = this.requirements,
    postSentenceSupervisionRequirements = this.postSentenceSupervisionRequirements,
    expectedEndDate = this.expectedEndDate,
  )
}
