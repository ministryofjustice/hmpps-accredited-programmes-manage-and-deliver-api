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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LearningNeeds
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getLatestCompletedLayerThreeAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAssessmentTimelineFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysHealthFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysLearningFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRoshFullFactory
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
      assertThat(response).hasFieldOrProperty("offenderGroupReconviction")
      assertThat(response).hasFieldOrProperty("offenderViolencePredictor")
      assertThat(response).hasFieldOrProperty("sara")
      assertThat(response).hasFieldOrProperty("riskOfSeriousRecidivism")
      assertThat(response).hasFieldOrProperty("riskOfSeriousHarm")
      assertThat(response).hasFieldOrProperty("alerts")
      assertThat(response).hasFieldOrProperty("dateRetrieved")
      assertThat(response).hasFieldOrProperty("lastUpdated")
    }
  }

  @Test
  fun `should return 404 when random crn and no assessment found`() {
    val crn = randomCrn()
    oasysApiStubs.stubNotFoundAssessmentsResponse(crn)
    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/$crn/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
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
  fun `should return 404 when no Alerts found for a crn`() {
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

  @Test
  fun `should return 400 when crn is not in the correct format`() {
    val response = performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/risks-and-needs/${randomAlphanumericString(6)}/risks-and-alerts",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.BAD_REQUEST.value(),
    )

    assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
  }

  @Nested
  @DisplayName("Get Learning Needs section")
  inner class GetLearningNeeds {
    @Test
    fun `should return learning needs section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysLearning = OasysLearningFactory().withCrn(referralEntity.crn).produce()
      oasysApiStubs.stubSuccessfulOasysLearningResponse(assessmentId, oasysLearning)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/learning-needs",
        returnType = object : ParameterizedTypeReference<LearningNeeds>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("workRelatedSkills")
      assertThat(response).hasFieldOrProperty("problemsReadWriteNum")
      assertThat(response).hasFieldOrProperty("learningDifficulties")
      assertThat(response).hasFieldOrProperty("problemAreas")
      assertThat(response).hasFieldOrProperty("qualifications")
      assertThat(response).hasFieldOrProperty("basicSkillsScore")
      assertThat(response).hasFieldOrProperty("eTEIssuesDetails")

      assertThat(response.workRelatedSkills).isEqualTo("Limited recent work history")
      assertThat(response.problemsReadWriteNum).isEqualTo("Difficulty with numeracy")
      assertThat(response.learningDifficulties).isEqualTo("ADHD")
      assertThat(response.problemAreas).contains("Difficulty with concentration")
      assertThat(response.qualifications).isEqualTo("NVQ Level 2")
      assertThat(response.basicSkillsScore).isEqualTo("3")
      assertThat(response.eTEIssuesDetails).isEqualTo("ete issues")
    }

    @Test
    fun `should return 404 when providing an unknown crn for learning needs and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/learning-needs",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no Learning Needs found for section4 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysLearningResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/learning-needs",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve LearningNeeds data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section4: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format for learning needs`() {
      // Given & When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/learning-needs",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  @DisplayName("Get health info")
  inner class GetHealthInfo {
    @Test
    fun `should return health section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysHealth = OasysHealthFactory().withCrn(referralEntity.crn).withGeneralHealth("Yes")
        .withGeneralHeathSpecify("In good health").produce()
      oasysApiStubs.stubSuccessfulOasysHealthResponse(assessmentId, oasysHealth)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/health",
        returnType = object : ParameterizedTypeReference<Health>() {},
      )

      assertThat(response.anyHealthConditions).isTrue
      assertThat(response.description).isEqualTo("In good health")
    }

    @Test
    fun `should return 404 when providing an unknown crn for health and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/health",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no health found for section4 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysHealthResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/health",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve Health data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section13: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format`() {
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/health",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  @DisplayName("Get ROSH Analysis section")
  inner class GetRoshAnalysis {
    @Test
    fun `should return Rosh analysis section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val timeline = assessment.getLatestCompletedLayerThreeAssessment()
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysRoshFull = OasysRoshFullFactory().produce()
      oasysApiStubs.stubSuccessfulOasysRoshFullResponse(timeline!!.id, oasysRoshFull)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/rosh-analysis",
        returnType = object : ParameterizedTypeReference<RoshAnalysis>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("assessmentCompleted")
      assertThat(response).hasFieldOrProperty("offenceDetails")
      assertThat(response).hasFieldOrProperty("whereAndWhen")
      assertThat(response).hasFieldOrProperty("howDone")
      assertThat(response).hasFieldOrProperty("whoVictims")
      assertThat(response).hasFieldOrProperty("anyoneElsePresent")
      assertThat(response).hasFieldOrProperty("whyDone")
      assertThat(response).hasFieldOrProperty("sources")
      assertThat(response).hasFieldOrProperty("identifyBehavioursIncidents")
      assertThat(response).hasFieldOrProperty("analysisBehaviourIncidents")

      assertThat(response.assessmentCompleted).isEqualTo(timeline.completedAt!!.toLocalDate())
      assertThat(response.offenceDetails).isEqualTo("Ms Puckett admits he went to Mr X's address on 23rd march 2010. She went there in order to buy cannabis.")
      assertThat(response.whereAndWhen).isEqualTo("At the victim's home address, in the evening")
      assertThat(response.howDone).isEqualTo("Appears to have been unprovoked violence - although basis of plea indicates otherwise. In any event this was impulsive, excessive violence using a weapon (metal pole)")
      assertThat(response.whoVictims).contains("Male-outnumbered by Mr Manette and his associate (not charged)")
      assertThat(response.anyoneElsePresent).isEqualTo("See above - Mr Manette was in the company of another, although he was not apprehended or charged for this offence.")
      assertThat(response.sources).isEqualTo("Interview, CPS documentation, basis of plea.")
      assertThat(response.identifyBehavioursIncidents).isEqualTo("Physical assault on cellmate requiring medical attention on 22nd March 2024. Weapon possession (improvised blade) discovered during cell search on 8th February 2024.")
      assertThat(response.analysisBehaviourIncidents).isEqualTo("Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention.")
    }

    @Test
    fun `should return 404 when providing an unknown crn for Rosh Analysis and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/rosh-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no Rosh Analysis found for sectionroshfull of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysRoshFullResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/rosh-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve RoshFull data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/sectionroshfull: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format for rosh analysis`() {
      // Given & When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/rosh-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }
}
