package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi

import io.kotest.assertions.fail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.AssessmentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.RsrScoreSource
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreType
import java.math.BigDecimal

class AssessRiskAndNeedsApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var assessRiskAndNeedsApiClient: AssessRiskAndNeedsApiClient

  @Test
  fun `should return OGRS4 risk predictors (version 2) for known assessment id`() {
    // Given
    stubAuthTokenEndpoint()
    val assessmentPk = 2114585L
    arnsApiStubs.stubSuccessfulRiskPredictorsResponse(assessmentPk)

    // When
    when (val response = assessRiskAndNeedsApiClient.getRiskPredictors(assessmentPk)) {
      // Then
      is ClientResult.Success -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body
        assertThat(body.outputVersion).isEqualTo("2") // version 2 denotes the newer OGRS4
        assertThat(body.status).isEqualTo(AssessmentStatus.COMPLETE)

        val versioned = body as AllPredictorVersionedDto
        val output = versioned.output
        assertThat(output?.combinedSeriousReoffendingPredictor?.algorithmVersion).isEqualTo("1.0")
        assertThat(output?.allReoffendingPredictor?.band).isEqualTo(ScoreLevel.MEDIUM)
        assertThat(output?.violentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
        assertThat(output?.seriousViolentReoffendingPredictor?.score).isEqualTo(BigDecimal("34"))
        assertThat(output?.indirectImageContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal("46"))
        assertThat(output?.indirectImageContactSexualReoffendingPredictor?.band).isEqualTo(ScoreLevel.MEDIUM)
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> fail(
        """
          Unexpected status code result:
          Method: ${response.method}
          Path: ${response.path}
          Status: ${response.status}
          Body: ${response.body}
        """.trimIndent(),
      )
    }
  }

  @Test
  fun `should return legacy risk predictors (version 1) for known assessment id`() {
    // Given
    val assessmentPk = 2114584L
    stubAuthTokenEndpoint()
    arnsApiStubs.stubSuccessfulLegacyRiskPredictorsResponse(assessmentPk)

    // When
    when (val response = assessRiskAndNeedsApiClient.getRiskPredictors(assessmentPk)) {
      // Then
      is ClientResult.Success -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body
        assertThat(body.outputVersion).isEqualTo("1") // version 1 denotes the legacy OGRS3
        assertThat(body.status).isEqualTo(AssessmentStatus.COMPLETE)

        val versioned = body as AllPredictorVersionedLegacyDto
        val output = versioned.output
        // groupReconvictionScore
        assertThat(output?.groupReconvictionScore?.oneYear).isEqualTo(BigDecimal.valueOf(29))
        assertThat(output?.groupReconvictionScore?.twoYears).isEqualTo(BigDecimal.valueOf(42))
        assertThat(output?.groupReconvictionScore?.scoreLevel).isEqualTo(ScoreLevel.MEDIUM)

        // violencePredictorScore (OVP)
        assertThat(output?.violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal.valueOf(34))
        assertThat(output?.violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal.valueOf(11))
        assertThat(output?.violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal.valueOf(45))
        assertThat(output?.violencePredictorScore?.oneYear).isEqualTo(BigDecimal.valueOf(23))
        assertThat(output?.violencePredictorScore?.twoYears).isEqualTo(BigDecimal.valueOf(36))
        assertThat(output?.violencePredictorScore?.ovpRisk).isEqualTo(ScoreLevel.MEDIUM)

        // generalPredictorScore (OGP)
        assertThat(output?.generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal.valueOf(38))
        assertThat(output?.generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal.valueOf(12))
        assertThat(output?.generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal.valueOf(46))
        assertThat(output?.generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal.valueOf(29))
        assertThat(output?.generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal.valueOf(39))
        assertThat(output?.generalPredictorScore?.ogpRisk).isEqualTo(ScoreLevel.MEDIUM)

        // riskOfSeriousRecidivismScore (RSR)
        assertThat(output?.riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal("3.45"))
        assertThat(output?.riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(output?.riskOfSeriousRecidivismScore?.source).isEqualTo(RsrScoreSource.OASYS)
        assertThat(output?.riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("4")

        // sexualPredictorScore (OSP)
        assertThat(output?.sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal("0.11"))
        assertThat(output?.sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal.valueOf(2))
        assertThat(output?.sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(ScoreLevel.LOW)
        assertThat(output?.sexualPredictorScore?.ospContactScoreLevel).isEqualTo(ScoreLevel.HIGH)
      }
      is ClientResult.Failure.Other<*> -> {
        response.exception.printStackTrace()
        fail("Unexpected client result: ${response::class.simpleName}")
      }

      is ClientResult.Failure.StatusCode<*> -> {
        println("Failed with status code ${response.status}")
        fail(
          """
            Unexpected status code result:
            Method: ${response.method}
            Path: ${response.path}
            Status: ${response.status}
            Body: ${response.body}
          """.trimIndent(),
        )
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown assessment id`() {
    // Given
    val unknownAssessmentPk = 99999999L
    stubAuthTokenEndpoint()
    arnsApiStubs.stubNotFoundRiskPredictorsResponse(unknownAssessmentPk)

    // When
    when (val response = assessRiskAndNeedsApiClient.getRiskPredictors(unknownAssessmentPk)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }
}
