package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralSentenceReferenceRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomEventId
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom.REQUIREMENT

class ReferralSentenceReferenceRequestFactory {
  private var sourcedFrom: ReferralEntitySourcedFrom = REQUIREMENT
  private var eventId: String = randomEventId(10).toString()
  private var additionalDetails: String = "Updating the sentence reference following repoint of the sentence"

  fun withSourcedFrom(sourcedFrom: ReferralEntitySourcedFrom) = apply { this.sourcedFrom = sourcedFrom }
  fun withEventId(eventId: String) = apply { this.eventId = eventId }
  fun withAdditionalDetails(additionalDetails: String) = apply { this.additionalDetails = additionalDetails }

  fun produce() = ReferralSentenceReferenceRequest(
    sourcedFrom = this.sourcedFrom,
    eventId = this.eventId,
    additionalDetails = this.additionalDetails,
  )
}
