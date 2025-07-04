package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferApi.model.ReferralDetails
import java.util.UUID

@Component
class FindAndReferApiClient(
  @Qualifier("findAndReferApiWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferral(referralId: UUID) = getRequest<ReferralDetails>("Find And Refer API") {
    log.debug("Retrieving referral details for referral id: $referralId")
    path = "/referral/$referralId"
  }
}
