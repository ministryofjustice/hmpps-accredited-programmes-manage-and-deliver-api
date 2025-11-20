package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescriptionList

class NDeliusApiProbationDeliveryUnitWithOfficeLocationsFactory {
  fun produce(
    code: String? = null,
    description: String? = null,
    officeLocations: List<CodeDescription>? = null,
  ): NDeliusApiProbationDeliveryUnitWithOfficeLocations = NDeliusApiProbationDeliveryUnitWithOfficeLocations(
    code = code ?: randomUppercaseString(2),
    description = description ?: randomSentence(1..2),
    officeLocations = officeLocations ?: createCodeDescriptionList(3),
  )
}
