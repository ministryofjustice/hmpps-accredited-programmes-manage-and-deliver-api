package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.NDeliusRegistrationsFactory

class NDeliusApiStubs(
  val wiremock: WireMockExtension,
  val objectMapper: ObjectMapper,
) {
  fun stubAccessCheck(granted: Boolean, vararg crns: String) {
    val response = LimitedAccessOffenderCheckResponse(
      crns.map { crn ->
        LimitedAccessOffenderCheck(
          crn = crn,
          userExcluded = !granted,
          userRestricted = false,
          exclusionMessage = null,
          restrictionMessage = null,
        )
      },
    )

    wiremock.stubFor(
      post(urlPathTemplate("/user/{username}/access"))
        .withRequestBody(
          matchingJsonPath("$"), // accept array or object body
        )
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

  fun stubPersonalDetailsResponseForCrn(
    crn: String,
    personalDetails: NDeliusPersonalDetails,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(personalDetails)),
        ),
    )
  }

  fun stubSuccessfulOffencesResponse(
    crn: String,
    eventNumber: String,
    offences: Offences = OffencesFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(offences)),
        ),
    )
  }

  fun stubNotFoundOffencesResponse(crn: String, eventNumber: String) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulSentenceInformationResponse(
    crn: String,
    eventNumber: Int?,
    nDeliusSentenceResponse: NDeliusSentenceResponse = NDeliusSentenceResponseFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/$crn/sentence/$eventNumber"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(nDeliusSentenceResponse)),
        ),
    )
  }

  fun stubNotFoundSentenceInformationResponse(
    crn: String,
    eventNumber: String,
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/$crn/sentence/$eventNumber"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulNDeliusRegistrationsResponse(
    nomisIdOrCrn: String,
    nDeliusRegistrations: NDeliusRegistrations = NDeliusRegistrationsFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/$nomisIdOrCrn/registrations"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(nDeliusRegistrations)),
        ),
    )
  }

  fun stubNotFoundNDeliusRegistrationsResponse(
    nomisIdOrCrn: String,
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/case/$nomisIdOrCrn/registrations"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulRequirementManagerResponse(
    crn: String,
    requirementId: String,
    requirementResponse: NDeliusCaseRequirementOrLicenceConditionResponse,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/requirement/$requirementId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(requirementResponse)),
        ),
    )
  }

  fun stubNotFoundRequirementManagerResponse(
    crn: String,
    requirementId: String,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/requirement/$requirementId"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulLicenceConditionManagerResponse(
    crn: String,
    licenceConditionId: String,
    licenceConditionResponse: NDeliusCaseRequirementOrLicenceConditionResponse,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/licence-conditions/$licenceConditionId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(licenceConditionResponse)),
        ),
    )
  }

  fun stubNotFoundLicenceConditionManagerResponse(
    crn: String,
    licenceConditionId: String,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/licence-conditions/$licenceConditionId"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }
}
