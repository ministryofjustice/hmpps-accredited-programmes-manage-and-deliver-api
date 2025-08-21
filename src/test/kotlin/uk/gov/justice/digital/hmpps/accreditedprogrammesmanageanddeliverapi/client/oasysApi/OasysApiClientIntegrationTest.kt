package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.ScoredAnswer
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAccommodation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysHealth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAccommodationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysHealthFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysLearningFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs

class OasysApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var oasysApiClient: OasysApiClient

  private lateinit var oasysApiStubs: OasysApiStubs

  @BeforeEach
  fun setup() {
    oasysApiStubs = OasysApiStubs(wiremock, objectMapper)
  }

  @Test
  fun `should return a pni calculation for known crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    oasysApiStubs.stubSuccessfulPniResponse(crn)

    // When
    when (val response = oasysApiClient.getPniCalculation(crn)) {
      // Then
      is ClientResult.Success<*> -> {
        assertThat(response.body).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val pniResponse = response.body as PniResponse
        assertThat(pniResponse).isNotNull()
        assertThat(pniResponse.pniCalculation?.pni).isEqualTo(Type.H)
        assertThat(pniResponse.assessment?.offenderAge).isEqualTo(32)
        assertThat(pniResponse.assessment?.questions?.impulsivity?.score).isEqualTo(ScoredAnswer.Problem.NONE.score)
        assertThat(pniResponse.assessment?.questions?.hostileOrientation?.score).isEqualTo(ScoredAnswer.Problem.SOME.score)
        assertThat(pniResponse.assessment?.questions?.sexualPreOccupation?.score).isEqualTo(ScoredAnswer.Problem.SIGNIFICANT.score)
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = randomAlphanumericString()
    oasysApiStubs.stubNotFoundPniResponse(crn)

    // When
    when (val response = oasysApiClient.getPniCalculation(crn)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }

  @Test
  fun `should return oasys learning section for known assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 12345L
    oasysApiStubs.stubSuccessfulOasysLearningResponse(
      assessmentId,
      OasysLearningFactory()
        .withCrn("X123456")
        .produce(),
    )

    // When
    when (val response = oasysApiClient.getLearning(assessmentId)) {
      // Then
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        println("Response body: ${response.body}")
        println("Response body class: ${response.body?.javaClass}")
        val oasysLearning = response.body as OasysLearning
        assertThat(oasysLearning.workRelatedSkills).isEqualTo("Limited recent work history")
        assertThat(oasysLearning.problemsReadWriteNum).isEqualTo("Difficulty with numeracy")
        assertThat(oasysLearning.learningDifficulties).isEqualTo("ADHD")
        assertThat(oasysLearning.problemAreas).isEqualTo(listOf("Difficulty with concentration"))
        assertThat(oasysLearning.qualifications).isEqualTo("NVQ Level 2")
        assertThat(oasysLearning.basicSkillsScore).isEqualTo("3")
        assertThat(oasysLearning.crn).isEqualTo("X123456")
        assertThat(oasysLearning.eTEIssuesDetails).isEqualTo("ete issues")
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown learning assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 9999999L
    oasysApiStubs.stubNotFoundOasysLearningResponse(assessmentId)

    // When
    when (val response = oasysApiClient.getLearning(assessmentId)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }

  @Test
  fun `should return oasys health section for known assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 12345L
    oasysApiStubs.stubSuccessfulOasysHealthResponse(
      assessmentId,
      OasysHealthFactory()
        .withCrn("X123456")
        .withGeneralHealth("Yes")
        .withGeneralHeathSpecify("All is well")
        .produce(),
    )

    // When
    when (val response = oasysApiClient.getHealth(assessmentId)) {
      // Then
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        println("Response body: ${response.body}")
        println("Response body class: ${response.body?.javaClass}")
        val oasysHealth = response.body as OasysHealth
        assertThat(oasysHealth.generalHealth).isEqualTo("Yes")
        assertThat(oasysHealth.crn).isEqualTo("X123456")
        assertThat(oasysHealth.generalHeathSpecify).isEqualTo("All is well")
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown health assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 9999999L
    oasysApiStubs.stubNotFoundOasysHealthResponse(assessmentId)

    // When
    when (val response = oasysApiClient.getHealth(assessmentId)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }

  @Test
  fun `should return oasys accommodation section for known assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 12345L
    oasysApiStubs.stubSuccessfulOasysAccommodationResponse(
      assessmentId,
      OasysAccommodationFactory().produce(),
    )

    // When
    when (val response = oasysApiClient.getAccommodation(assessmentId)) {
      // Then
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val oasysAccommodation = response.body as OasysAccommodation
        assertThat(oasysAccommodation.noFixedAbodeOrTransient).isEqualTo("Yes")
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown accommodation assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentId = 9999999L
    oasysApiStubs.stubNotFoundOasysAccommodationResponse(assessmentId)

    // When
    when (val response = oasysApiClient.getAccommodation(assessmentId)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }
}
