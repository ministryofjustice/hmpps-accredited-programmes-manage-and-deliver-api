package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getLatestCompletedLayerThreeAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAssessmentTimelineFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs

class RisksAndNeedsControllerIntegrationTest : IntegrationTestBase() {

  private lateinit var oasysApiStubs: OasysApiStubs

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    oasysApiStubs = OasysApiStubs(wiremock, objectMapper)
    nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
    stubAuthTokenEndpoint()
  }

  @Nested
  @DisplayName("Get Risks and Alerts section")
  inner class GetRisksAndAlerts {
    @Test
    fun `should return risks and alerts section`() {
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubSuccessfulOasysOffendingInfoResponse(assessmentId)
      oasysApiStubs.stubSuccessfulOasysRelationshipsResponse(assessmentId)
      oasysApiStubs.stubSuccessfulOasysRoshSummaryResponse(assessmentId)
      oasysApiStubs.stubSuccessfulOasysRiskPredictorScores(assessmentId)
      nDeliusApiStubs.stubSuccessfulNDeliusRegistrationsResponse(referralEntity.crn)

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
        returnType = object : ParameterizedTypeReference<Risks>() {},
      )

      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("ogrsYear1")
      assertThat(response).hasFieldOrProperty("ogrsYear2")
      assertThat(response).hasFieldOrProperty("ogrsYear1")
      assertThat(response).hasFieldOrProperty("ogrsYear2")
      assertThat(response).hasFieldOrProperty("ogrsRisk")
      assertThat(response).hasFieldOrProperty("ovpYear1")
      assertThat(response).hasFieldOrProperty("ovpYear2")
      assertThat(response).hasFieldOrProperty("ovpRisk")
      assertThat(response).hasFieldOrProperty("saraScorePartner")
      assertThat(response).hasFieldOrProperty("saraScoreOthers")
      assertThat(response).hasFieldOrProperty("rsrScore")
      assertThat(response).hasFieldOrProperty("rsrRisk")
      assertThat(response).hasFieldOrProperty("ospcScore")
      assertThat(response).hasFieldOrProperty("ospiScore")
      assertThat(response).hasFieldOrProperty("overallRoshLevel")
      assertThat(response).hasFieldOrProperty("riskPrisonersCustody")
      assertThat(response).hasFieldOrProperty("riskStaffCustody")
      assertThat(response).hasFieldOrProperty("riskKnownAdultCustody")
      assertThat(response).hasFieldOrProperty("riskPublicCustody")
      assertThat(response).hasFieldOrProperty("riskChildrenCustody")
      assertThat(response).hasFieldOrProperty("riskStaffCommunity")
      assertThat(response).hasFieldOrProperty("riskKnownAdultCommunity")
      assertThat(response).hasFieldOrProperty("riskPublicCommunity")
      assertThat(response).hasFieldOrProperty("riskChildrenCommunity")
      assertThat(response).hasFieldOrProperty("alerts")
    }
  }

  @Test
  fun `should return 404 when random crn and no assessment found`() {
    val nomisIdOrCrn = randomAlphanumericString(6)
    oasysApiStubs.stubNotFoundAssessmentsResponse(nomisIdOrCrn)
    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/$nomisIdOrCrn/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("No assessment found for nomisIdOrCrn: $nomisIdOrCrn")
  }

  @Test
  fun `should return 404 when no Offending Info found for section1 of assessment`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
    val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
    oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
    oasysApiStubs.stubNotFoundOasysOffendingInfoResponse(assessmentId)

    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("Failure to retrieve OffendingInfo data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section1: 404 NOT_FOUND'")
  }

  @Test
  fun `should return 404 when no Relationships found for section6 of assessment`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
    val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
    oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
    oasysApiStubs.stubSuccessfulOasysOffendingInfoResponse(assessmentId)
    oasysApiStubs.stubNotFoundOasysRelationshipsResponse(assessmentId)

    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("Failure to retrieve Relationships data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section6: 404 NOT_FOUND'")
  }

  @Test
  fun `should return 404 when no Rosh Summary found for sectionroshsumm of assessment`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
    val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
    oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
    oasysApiStubs.stubSuccessfulOasysOffendingInfoResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRelationshipsResponse(assessmentId)
    oasysApiStubs.stubNotFoundOasysOasysRoshSummaryResponse(assessmentId)

    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("Failure to retrieve RoshSummary data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/sectionroshsumm: 404 NOT_FOUND'")
  }

  @Test
  fun `should return 404 when no Risk Predictors found for risk-predictors of assessment`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
    val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
    oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
    oasysApiStubs.stubSuccessfulOasysOffendingInfoResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRelationshipsResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRoshSummaryResponse(assessmentId)
    oasysApiStubs.stubNotFoundOasysRiskPredictorScoresResponse(assessmentId)

    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("Failure to retrieve RiskPredictors data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/risk-predictors: 404 NOT_FOUND'")
  }

  @Test
  fun `should return 404 when no Alerts found for a nomisIdOrCrn`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
    val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
    oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
    oasysApiStubs.stubSuccessfulOasysOffendingInfoResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRelationshipsResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRoshSummaryResponse(assessmentId)
    oasysApiStubs.stubSuccessfulOasysRiskPredictorScores(assessmentId)
    nDeliusApiStubs.stubNotFoundNDeliusRegistrationsResponse(referralEntity.crn)

    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${referralEntity.crn}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("Failure to retrieve ActiveAlerts for crn: ${referralEntity.crn}, reason: 'Unable to complete GET request to /case/${referralEntity.crn}/registrations: 404 NOT_FOUND'")
  }
}
