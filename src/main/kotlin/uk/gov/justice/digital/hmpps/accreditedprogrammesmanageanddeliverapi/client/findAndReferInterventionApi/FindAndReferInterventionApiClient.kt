package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.ReferralDetails
import java.util.UUID

@Component
class FindAndReferInterventionApiClient(
  @Qualifier("findAndReferInterventionApiWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferral(referralId: UUID) = getRequest<ReferralDetails>("Find And Refer Intervention API") {
    path = "/referral/$referralId"
  }
}
