package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniRiskLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SaraRisk
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class PniControllerIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
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
    val sexDomainScore = response.domainScores.sexDomainScore
    assertThat(sexDomainScore.overallSexDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(sexDomainScore.individualSexScores.sexualPreOccupation).isEqualTo(2)
    assertThat(sexDomainScore.individualSexScores.offenceRelatedSexualInterests).isEqualTo(1)
    assertThat(sexDomainScore.individualSexScores.emotionalCongruence).isEqualTo(1)

    // Thinking Domain assertions
    val thinkingDomainScore = response.domainScores.thinkingDomainScore
    assertThat(thinkingDomainScore.overallThinkingDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(thinkingDomainScore.individualThinkingScores.proCriminalAttitudes).isEqualTo(1)
    assertThat(thinkingDomainScore.individualThinkingScores.hostileOrientation).isEqualTo(1)

    // Relationship Domain assertions
    val relationshipDomainScore = response.domainScores.relationshipDomainScore
    assertThat(relationshipDomainScore.overallRelationshipDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(relationshipDomainScore.individualRelationshipScores.curRelCloseFamily).isEqualTo(0)
    assertThat(relationshipDomainScore.individualRelationshipScores.prevCloseRelationships).isEqualTo(0)
    assertThat(relationshipDomainScore.individualRelationshipScores.easilyInfluenced).isEqualTo(0)
    assertThat(relationshipDomainScore.individualRelationshipScores.aggressiveControllingBehaviour).isEqualTo(0)

    // Self-Management Domain assertions
    val selfManagementDomainScore = response.domainScores.selfManagementDomainScore
    assertThat(selfManagementDomainScore.overallSelfManagementDomainLevel).isEqualTo(NeedLevel.HIGH_NEED)
    assertThat(selfManagementDomainScore.individualSelfManagementScores.impulsivity).isEqualTo(0)
    assertThat(selfManagementDomainScore.individualSelfManagementScores.temperControl).isEqualTo(0)
    assertThat(selfManagementDomainScore.individualSelfManagementScores.problemSolvingSkills).isEqualTo(0)
    assertThat(selfManagementDomainScore.individualSelfManagementScores.difficultiesCoping).isNull()

    // Risk score assertions

    assertThat(response.riskScore.classification).isEqualTo(PniRiskLevel.HIGH_RISK.name)

    val individualRiskScores = response.riskScore.individualRiskScores
    assertThat(individualRiskScores.ogrs3Risk).isNull()
    assertThat(individualRiskScores.ovpRisk).isNull()
    assertThat(individualRiskScores.ospDc).isNull()
    assertThat(individualRiskScores.ospIic).isNull()
    assertThat(individualRiskScores.rsr).isNull()

    val sara = individualRiskScores.sara
    assertThat(sara?.highestRisk).isEqualTo(SaraRisk.MEDIUM)
    assertThat(sara?.saraRiskOfViolenceTowardsPartner).isEqualTo("Not Applicable")
    assertThat(sara?.saraRiskOfViolenceTowardsOthers).isEqualTo("Medium")
    assertThat(sara?.assessmentId).isEqualTo("10082385")
  }

  @Test
  fun `should return empty PNI score when retrieving PNI score for an unknown CRN`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN_CRN"
    oasysApiStubs.stubNotFoundPniResponse(crn)

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/pni-score/$crn",
      object : ParameterizedTypeReference<PniScore>() {},
    )

    // Then
    assertThat(response).isNotNull
    assertThat(response).isEqualTo(PniScore.empty())
    assertThat(response.overallIntensity).isEqualTo(OverallIntensity.MISSING_INFORMATION)
    assertThat(response.domainScores).isNotNull
    assertThat(response.domainScores.sexDomainScore).isNotNull
    assertThat(response.domainScores.sexDomainScore.individualSexScores).isNotNull
    assertThat(response.domainScores.sexDomainScore.individualSexScores.sexualPreOccupation).isNull()
    assertThat(response.domainScores.sexDomainScore.overallSexDomainLevel).isNull()
    assertThat(response.domainScores.thinkingDomainScore).isNotNull
    assertThat(response.domainScores.thinkingDomainScore.individualThinkingScores).isNotNull
    assertThat(response.domainScores.thinkingDomainScore.individualThinkingScores.proCriminalAttitudes).isNull()
    assertThat(response.domainScores.thinkingDomainScore.overallThinkingDomainLevel).isNull()
    assertThat(response.domainScores.relationshipDomainScore).isNotNull
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores).isNotNull
    assertThat(response.domainScores.relationshipDomainScore.individualRelationshipScores.easilyInfluenced).isNull()
    assertThat(response.domainScores.relationshipDomainScore.overallRelationshipDomainLevel).isNull()
    assertThat(response.domainScores.selfManagementDomainScore).isNotNull
    assertThat(response.domainScores.selfManagementDomainScore.overallSelfManagementDomainLevel).isNull()
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores).isNotNull
    assertThat(response.domainScores.selfManagementDomainScore.individualSelfManagementScores.problemSolvingSkills).isNull()
    assertThat(response.riskScore).isNotNull
    assertThat(response.riskScore.individualRiskScores).isNotNull
    assertThat(response.riskScore.individualRiskScores.rsr).isNull()
    assertThat(response.riskScore.individualRiskScores.sara).isNotNull
    assertThat(response.riskScore.individualRiskScores.sara?.highestRisk).isNull()
    assertThat(response.riskScore.individualRiskScores.ovpRisk).isNull()
    assertThat(response.riskScore.individualRiskScores.ospDc).isNull()
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
