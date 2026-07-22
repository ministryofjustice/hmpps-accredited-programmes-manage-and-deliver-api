package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccess

private const val PROBATION_ACCESS_CONTROL_API = "PROBATION ACCESS CONTROL API"

@Component
class ProbationAccessControlApiClient(
  @Qualifier("probationAccessControlWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getCaseAccessByCrn(crn: String) = getRequest<AllCaseAccess>(PROBATION_ACCESS_CONTROL_API) {
    path = "/case/$crn/access"
  }
}
