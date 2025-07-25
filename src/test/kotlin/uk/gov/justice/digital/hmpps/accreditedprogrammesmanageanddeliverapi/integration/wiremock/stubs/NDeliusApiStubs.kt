package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory

class NDeliusApiStubs(
  val wiremock: WireMockExtension,
  val objectMapper: ObjectMapper,
) {
  fun stubAccessCheck(granted: Boolean, crn: String = "X933590") {
    val response = LimitedAccessOffenderCheckResponse(
      listOf(
        LimitedAccessOffenderCheck(
          crn = crn,
          userExcluded = !granted,
          userRestricted = false,
          exclusionMessage = null,
          restrictionMessage = null,
        ),
      ),
    )

    wiremock.stubFor(
      post(urlPathTemplate("/user/{username}/access"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubPersonalDetailsResponse(
    nDeliusPersonalDetails: NDeliusPersonalDetails? = NDeliusPersonalDetailsFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/{crn}/personal-details"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(nDeliusPersonalDetails)),
        ),
    )
  }
}
