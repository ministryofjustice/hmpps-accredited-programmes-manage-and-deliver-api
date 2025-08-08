package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString

object Utils {

  fun createCodeDescription(): CodeDescription = CodeDescription(randomUppercaseString(2), randomSentence())
  fun createCodeDescriptionList(listSize: Int = 1): List<CodeDescription> = (1..listSize).map { createCodeDescription() }
}
