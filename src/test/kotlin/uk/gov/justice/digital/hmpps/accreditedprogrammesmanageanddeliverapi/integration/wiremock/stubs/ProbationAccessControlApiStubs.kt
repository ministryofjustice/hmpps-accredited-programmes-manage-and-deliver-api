package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccess
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccessUsernameRange
import java.time.OffsetDateTime

@TestComponent
class ProbationAccessControlApiStubs {

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  fun stubCaseAccessByCrn(
    crn: String,
    excludedFrom: List<AllCaseAccessUsernameRange> = emptyList(),
    restrictedTo: List<AllCaseAccessUsernameRange> = emptyList(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/access"))
        .atPriority(1)
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                AllCaseAccess(
                  crn = crn,
                  excludedFrom = excludedFrom,
                  restrictedTo = restrictedTo,
                ),
              ),
            ),
        ),
    )
  }

  fun stubOpenAccessForAnyCrn() {
    wiremock.stubFor(
      get(urlPathMatching("/case/.+/access"))
        .atPriority(10)
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                AllCaseAccess(
                  crn = "UNKNOWN",
                  excludedFrom = emptyList(),
                  restrictedTo = emptyList(),
                ),
              ),
            ),
        ),
    )
  }

  fun stubOpenAccessByCrns(vararg crns: String) {
    crns.forEach { stubCaseAccessByCrn(it) }
  }

  fun usernameRange(username: String = "AUTH_ADM"): AllCaseAccessUsernameRange = AllCaseAccessUsernameRange(
    username = username,
    since = OffsetDateTime.now().minusDays(1),
    until = null,
  )
}
