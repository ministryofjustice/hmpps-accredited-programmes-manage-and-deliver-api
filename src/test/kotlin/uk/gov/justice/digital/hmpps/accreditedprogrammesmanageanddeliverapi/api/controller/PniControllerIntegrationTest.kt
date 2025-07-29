package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class PniControllerIntegrationTest : IntegrationTestBase() {

  private lateinit var oasysApiStubs: OasysApiStubs

  @BeforeEach
  fun setup() {
    oasysApiStubs = OasysApiStubs(wiremock, objectMapper)
  }

  @Test
  fun `should successfully retrieve PNI score for a known CRN`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    oasysApiStubs.stubSuccessfulPniResponse(crn)

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/pni-score/$crn",
      object : ParameterizedTypeReference<PniScore>() {},
    )

    // Then
    assertThat(response).isNotNull
    assertThat(response.overallIntensity).isEqualTo(OverallIntensity.HIGH)
    assertThat(response.domainScores).isNotNull

    // Sex Domain assertions
    assertThat(response.domainScores.sexDomainScore.overallSexDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(response.domainScores.sexDomainScore.individualSexScores.sexualPreOccupation).isEqualTo(2)
    assertThat(response.domainScores.sexDomainScore.individualSexScores.offenceRelatedSexualInterests).isEqualTo(1)
    assertThat(response.domainScores.sexDomainScore.individualSexScores.emotionalCongruence).isEqualTo(1)

    // Thinking Domain assertions
    assertThat(response.domainScores.thinkingDomainScore.overallThinkingDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(response.domainScores.thinkingDomainScore.individualThinkingScores.proCriminalAttitudes).isEqualTo(1)
    assertThat(response.domainScores.thinkingDomainScore.individualThinkingScores.hostileOrientation).isEqualTo(1)

    // Relationship Domain assertions
    assertThat(response.domainScores.relationshipDomainScore.overallRelationshipDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores.curRelCloseFamily).isEqualTo(0)
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores.prevCloseRelationships).isEqualTo(
      0,
    )
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores.easilyInfluenced).isEqualTo(0)
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores.aggressiveControllingBehaviour).isEqualTo(
      0,
    )

    // Self-Management Domain assertions
    assertThat(response.domainScores.selfManagementDomainScore.overallSelfManagementDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores.impulsivity).isEqualTo(0)
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores.temperControl).isEqualTo(0)
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores.problemSolvingSkills).isEqualTo(
      0,
    )
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores.difficultiesCoping).isNull()
  }

  @Test
  fun `should return HTTP 404 not found when retrieving PNI score for an unknown CRN`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN_CRN"
    oasysApiStubs.stubNotFoundPniResponse(crn)

    // When & Then
    performRequestAndExpectStatus(
      HttpMethod.GET,
      "/pni-score/$crn",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return HTTP 403 Forbidden when retrieving PNI score without the appropriate role`() {
    // Given
    val crn = "X123456"

    // When & Then
    webTestClient
      .method(HttpMethod.GET)
      .uri("/pni-score/$crn")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
