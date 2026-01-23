package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusRegionWithMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.NDeliusApiProbationDeliveryUnitWithOfficeLocationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.NDeliusRegistrationsFactory

@TestComponent
class NDeliusApiStubs {

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  fun clearAllStubs() {
    wiremock.resetAll()
  }

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

  fun stubNotFoundPersonalDetailsResponse(crn: String? = null) {
    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
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
    nDeliusSentenceResponse: NDeliusSentenceResponse? = null,
    sourcedFrom: ReferralEntitySourcedFrom? = null,
  ) {
    val nDeliusSentenceResponse = nDeliusSentenceResponse ?: NDeliusSentenceResponseFactory(sourcedFrom).produce()
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

  fun stubUserTeamsResponse(
    username: String,
    userTeams: NDeliusUserTeams? = NDeliusUserTeamsFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/user/$username/teams"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(userTeams)),
        ),
    )
  }

  fun stubRegionWithMembersResponse(
    regionCode: String? = randomUppercaseString(),
    regionWithMembers: NDeliusRegionWithMembers? = NDeliusRegionWithMembersFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/regions/$regionCode/members"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(regionWithMembers)),
        ),
    )
  }

  fun stubRegionPduOfficeLocationsResponse(
    pduCode: String? = randomUppercaseString(),
    pduWithOfficeLocations: NDeliusApiProbationDeliveryUnitWithOfficeLocations? = NDeliusApiProbationDeliveryUnitWithOfficeLocationsFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/regions/pdu/$pduCode/office-locations"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(pduWithOfficeLocations)),
        ),
    )
  }

  fun stubSuccessfulPostAppointmentsResponse() {
    wiremock.stubFor(
      post(urlEqualTo("/appointments"))
        .willReturn(
          aResponse()
            .withStatus(HttpStatus.CREATED.value()),
        ),
    )
  }

  fun stubSuccessfulDeleteAppointmentsResponse() {
    wiremock.stubFor(
      delete(urlEqualTo("/appointments"))
        .willReturn(
          aResponse()
            .withStatus(HttpStatus.NO_CONTENT.value()),
        ),
    )
  }
}
