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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.AlcoholMisuseDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.DrugDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LearningNeeds
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Relationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Timeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getLatestCompletedLayerThreeAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAlcoholMisuseDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAssessmentTimelineFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysDrugDetailFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysHealthFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysLearningFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysLifestyleAndAssociatesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysOffenceAnalysisFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRelationshipsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRoshFullFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import java.time.LocalDateTime

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
      oasysApiStubs.stubSuccessfulOasysAccommodationResponse(assessmentId)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/learning-needs",
        returnType = object : ParameterizedTypeReference<LearningNeeds>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("assessmentCompleted")
      assertThat(response).hasFieldOrProperty("noFixedAbodeOrTransient")
      assertThat(response).hasFieldOrProperty("workRelatedSkills")
      assertThat(response).hasFieldOrProperty("problemsReadWriteNum")
      assertThat(response).hasFieldOrProperty("learningDifficulties")
      assertThat(response).hasFieldOrProperty("problemAreas")
      assertThat(response).hasFieldOrProperty("qualifications")
      assertThat(response).hasFieldOrProperty("basicSkillsScore")
      assertThat(response).hasFieldOrProperty("basicSkillsScoreDescription")

      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
      assertThat(response.noFixedAbodeOrTransient).isTrue
      assertThat(response.workRelatedSkills).isEqualTo("Limited recent work history")
      assertThat(response.problemsReadWriteNum).isEqualTo("Difficulty with numeracy")
      assertThat(response.learningDifficulties).isEqualTo("ADHD")
      assertThat(response.problemAreas).contains("Difficulty with concentration")
      assertThat(response.qualifications).isEqualTo("NVQ Level 2")
      assertThat(response.basicSkillsScore).isEqualTo("3")
      assertThat(response.basicSkillsScoreDescription).isEqualTo("ete issues")
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
      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
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
    fun `should return 404 when no health found for section13 of assessment`() {
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
  @DisplayName("Get oasys relationships data")
  inner class GetRelationships {
    @Test
    fun `should return relationships section for known CRN`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysRelationships = OasysRelationshipsFactory()
        .withPrevOrCurrentDomesticAbuse("Yes")
        .withPrevCloseRelationships("2-Significant problems")
        .withEmotionalCongruence("0-No problems")
        .withVictimOfPartner("No")
        .withVictimOfFamily("Yes")
        .withPerpAgainstFamily("No")
        .withPerpAgainstPartner("No")
        .withRelIssuesDetails("This person has a history of domestic violence")
        .withRelCloseFamily("0-No problems")
        .withRelCurrRelationshipStatus("Not in a relationship")
        .withRelationshipWithPartner("0-No problems")
        .produce()
      oasysApiStubs.stubSuccessfulOasysRelationshipsResponse(assessmentId, oasysRelationships)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/relationships",
        returnType = object : ParameterizedTypeReference<Relationships>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("assessmentCompleted")
      assertThat(response).hasFieldOrProperty("dvEvidence")
      assertThat(response).hasFieldOrProperty("victimFormerPartner")
      assertThat(response).hasFieldOrProperty("victimFamilyMember")
      assertThat(response).hasFieldOrProperty("victimOfPartnerFamily")
      assertThat(response).hasFieldOrProperty("perpOfPartnerOrFamily")
      assertThat(response).hasFieldOrProperty("relIssuesDetails")
      assertThat(response).hasFieldOrProperty("relCloseFamily")
      assertThat(response).hasFieldOrProperty("relCurrRelationshipStatus")
      assertThat(response).hasFieldOrProperty("prevCloseRelationships")
      assertThat(response).hasFieldOrProperty("emotionalCongruence")
      assertThat(response).hasFieldOrProperty("relationshipWithPartner")
      assertThat(response).hasFieldOrProperty("prevOrCurrentDomesticAbuse")

      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
      assertThat(response.dvEvidence).isTrue
      assertThat(response.victimFormerPartner).isFalse
      assertThat(response.victimFamilyMember).isTrue
      assertThat(response.relIssuesDetails).isEqualTo("This person has a history of domestic violence")
      assertThat(response.prevOrCurrentDomesticAbuse).isEqualTo("Yes")
      assertThat(response.victimOfPartnerFamily).isFalse
      assertThat(response.perpOfPartnerOrFamily).isFalse
      assertThat(response.relCloseFamily).isEqualTo("0-No problems")
      assertThat(response.relCurrRelationshipStatus).isEqualTo("Not in a relationship")
      assertThat(response.prevCloseRelationships).isEqualTo("2-Significant problems")
      assertThat(response.emotionalCongruence).isEqualTo("0-No problems")
      assertThat(response.relationshipWithPartner).isEqualTo("0-No problems")
    }

    @Test
    fun `should return 404 when providing an unknown crn for relationships and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/relationships",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no relationships found for section6 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysRelationshipsResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/relationships",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve Relationships data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section6: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format for relationships`() {
      // Given & When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/relationships",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      // Then
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

  @Nested
  @DisplayName("Get drug details")
  inner class GetDrugDetail {

    @Test
    fun `should return drug detail section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)

      val timeline = listOf<Timeline>(Timeline(1L, "COMPLETE", "LAYER3", LocalDateTime.now()))
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).withTimeline(timeline).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysDrugDetail =
        OasysDrugDetailFactory().withCrn(referralEntity.crn).withLevelOfUseOfMainDrug("2 - Atleast once a week")
          .withDrugsMajorActivity("1 - Some problems").produce()
      oasysApiStubs.stubSuccessfulOasysDrugDetailResponse(assessmentId, oasysDrugDetail)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/drug-details",
        returnType = object : ParameterizedTypeReference<DrugDetails>() {},
      )

      assertThat(response.drugsMajorActivity).isEqualTo("1 - Some problems")
      assertThat(response.levelOfUseOfMainDrug).isEqualTo("2 - Atleast once a week")
      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
    }

    @Test
    fun `should return 404 when providing an unknown crn for drug detail and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/drug-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no drug detail found for section8 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysDrugDetailResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/drug-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve DrugDetail data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section8: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format`() {
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/drug-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  @DisplayName("Get lifestyle and associates")
  inner class GetLifestyleAndAssociates {

    @Test
    fun `should return lifestyle and associates detail section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)

      val timeline = listOf<Timeline>(Timeline(1L, "COMPLETE", "LAYER3", LocalDateTime.now()))
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).withTimeline(timeline).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysLifestyleAndAssociates =
        OasysLifestyleAndAssociatesFactory().withCrn(referralEntity.crn).produce()
      oasysApiStubs.stubSuccessfulOasysLifestyleAndAssociatesResponse(assessmentId, oasysLifestyleAndAssociates)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/lifestyle-and-associates",
        returnType = object : ParameterizedTypeReference<LifestyleAndAssociates>() {},
      )

      assertThat(response.regActivitiesEncourageOffending).isEqualTo("1 - Some problems")
      assertThat(response.lifestyleIssuesDetails).isEqualTo("There are issues around involvement with drugs")
      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
    }

    @Test
    fun `should return 404 when providing an unknown crn for lifestyle and associates and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/lifestyle-and-associates",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no lifestyle and associates found for section7 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysLifestyleAndAssociatesResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/lifestyle-and-associates",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve LifestyleAndAssociates data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section7: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format`() {
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/lifestyle-and-associates",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  @DisplayName("Get alcohol misuse details")
  inner class GetAlcoholMisuseDetails {

    @Test
    fun `should return alcohol misuse details section for known crn`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)

      val timeline = listOf(Timeline(1L, "COMPLETE", "LAYER3", LocalDateTime.now()))
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).withTimeline(timeline).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysAlcoholMisuseDetails = OasysAlcoholMisuseDetailsFactory().produce()
      oasysApiStubs.stubSuccessfulOasysAlcoholMisuseDetailsResponse(assessmentId, oasysAlcoholMisuseDetails)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/alcohol-misuse-details",
        returnType = object : ParameterizedTypeReference<AlcoholMisuseDetails>() {},
      )

      assertThat(response.currentUse).isEqualTo("1-Some problems")
      assertThat(response.bingeDrinking).isEqualTo("1-Some problems")
      assertThat(response.frequencyAndLevel).isEqualTo("2-Significant problems")
      assertThat(response.alcoholIssuesDetails).isEqualTo("Alcohol dependency affecting employment and relationships")
      assertThat(response.assessmentCompleted).isEqualTo(assessment.getLatestCompletedLayerThreeAssessment()?.completedAt?.toLocalDate())
    }

    @Test
    fun `should return 404 when providing an unknown crn for alcohol misuse details and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/alcohol-misuse-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no alcohol misuse details found for section9 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysAlcoholMisuseDetailsResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/alcohol-misuse-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve AlcoholMisuseDetails data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section9: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format`() {
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/alcohol-misuse-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  @DisplayName("Get Offence Analysis section")
  inner class GetOffenceAnalysis {
    @Test
    fun `should return Offence analysis section`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val timeline = assessment.getLatestCompletedLayerThreeAssessment()
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      val oasysOffenceAnalysis = OasysOffenceAnalysisFactory()
        .withWhatOccurred(
          listOf(
            OasysOffenceAnalysis.WhatOccurred.TARGETING.description,
            OasysOffenceAnalysis.WhatOccurred.RACIAL_MOTIVATED.description,
            OasysOffenceAnalysis.WhatOccurred.REVENGE.description,
            OasysOffenceAnalysis.WhatOccurred.PHYSICAL_VIOLENCE_TOWARDS_PARTNER.description,
            OasysOffenceAnalysis.WhatOccurred.REPEAT_VICTIMISATION.description,
            OasysOffenceAnalysis.WhatOccurred.VICTIM_WAS_STRANGER.description,
            OasysOffenceAnalysis.WhatOccurred.STALKING.description,
          ),
        )
        .produce()
      oasysApiStubs.stubSuccessfulOasysOffenceAnalysisResponse(timeline!!.id, oasysOffenceAnalysis)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/offence-analysis",
        returnType = object : ParameterizedTypeReference<OffenceAnalysis>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).hasFieldOrProperty("assessmentCompleted")
      assertThat(response).hasFieldOrProperty("briefOffenceDetails")
      assertThat(response).hasFieldOrProperty("victimsAndPartners")
      assertThat(response).hasFieldOrProperty("recognisesImpact")
      assertThat(response).hasFieldOrProperty("otherOffendersAndInfluences")
      assertThat(response).hasFieldOrProperty("motivationAndTriggers")
      assertThat(response).hasFieldOrProperty("responsibility")
      assertThat(response).hasFieldOrProperty("patternOfOffending")

      assertThat(response.assessmentCompleted).isEqualTo(timeline.completedAt!!.toLocalDate())
      assertThat(response.briefOffenceDetails).isEqualTo(oasysOffenceAnalysis.offenceAnalysis)
      assertThat(response.victimsAndPartners?.contactTargeting).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.raciallyMotivated).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.revenge).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.physicalViolenceTowardsPartner).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.repeatVictimisation).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.victimWasStranger).isEqualTo("Yes")
      assertThat(response.victimsAndPartners?.stalking).isEqualTo("Yes")
      assertThat(response.recognisesImpact).isEqualTo("No")
      assertThat(response.otherOffendersAndInfluences?.wereOtherOffendersInvolved).isEqualTo("Yes")
      assertThat(response.otherOffendersAndInfluences?.wasTheOffenderLeader).isEqualTo("No information available")
      assertThat(response.otherOffendersAndInfluences?.peerGroupInfluences).isEqualTo("No")
      assertThat(response.otherOffendersAndInfluences?.numberOfOthersInvolved).isEqualTo("2")
      assertThat(response.motivationAndTriggers).isEqualTo("Ms Puckett stated that as she had been attempting to address his long standing addiction to heroin, with the support of a Drug Rehabilitation Requirement as part of a community order, he had been using cannabis as a substitute in order to assuage symptoms of withdrawal or stress.")
      assertThat(response.responsibility?.acceptsResponsibility).isEqualTo("Yes")
      assertThat(response.responsibility?.acceptsResponsibilityDetail).isEqualTo("OPD Automatic screen in as first test")
      assertThat(response.patternOfOffending).isEqualTo("Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention.")
    }

    @Test
    fun `should return 404 when providing an unknown crn for Offence Analysis and no assessment exists`() {
      // Given
      val crn = randomCrn()
      oasysApiStubs.stubNotFoundAssessmentsResponse(crn)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/$crn/offence-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("No assessment found for crn: $crn")
    }

    @Test
    fun `should return 404 when no Offence Analysis found for section2 of assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val assessment = OasysAssessmentTimelineFactory().withCrn(referralEntity.crn).produce()
      val assessmentId = assessment.getLatestCompletedLayerThreeAssessment()!!.id
      oasysApiStubs.stubSuccessfulAssessmentsResponse(referralEntity.crn, assessment)
      oasysApiStubs.stubNotFoundOasysOffenceAnalysisResponse(assessmentId)

      // When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${referralEntity.crn}/offence-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.developerMessage).isEqualTo("Failure to retrieve OffenceAnalysis data for assessmentId: $assessmentId, reason: 'Unable to complete GET request to /assessments/$assessmentId/section/section2: 404 NOT_FOUND'")
    }

    @Test
    fun `should return 400 when crn is not in the correct format for offence analysis`() {
      // Given & When
      val response = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/risks-and-needs/${randomAlphanumericString(6)}/offence-analysis",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )
      // Then
      assertThat(response.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }
}
