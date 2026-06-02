package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory

@ExtendWith(MockitoExtension::class)
class PniServiceTest {

  @Mock
  private lateinit var oasysApiClient: OasysApiClient

  @InjectMocks
  private lateinit var pniService: PniService

  @Test
  fun `when getPniScore has pniCalculation in oasys return pniScore`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().produce()

    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(
        status = HttpStatus.OK,
        body = pniResponse,
      ),
    )

    val result = pniService.getPniCalculation(crn)
    assertThat(result).isNotNull
    assertThat(result.overallIntensity).isEqualTo(Type.toIntensity(pniResponse.pniCalculation?.pni))
  }

  @Test
  fun `when getPniScore returns 404 from oasys return empty pniScore`() {
    val crn = randomCrn()

    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Failure.StatusCode(
        HttpMethod.GET,
        "/assessments/pni/$crn?community=true",
        HttpStatus.NOT_FOUND,
        "",
      ),
    )

    val result = pniService.getPniCalculation(crn)
    assertThat(result).isEqualTo(PniScore.empty())
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.MISSING_INFORMATION)
  }

  @Test
  fun `when getPniScore returns non-404 failure from oasys returns empty pniScore`() {
    val crn = randomCrn()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Failure.StatusCode(
        HttpMethod.GET,
        "/assessments/pni/$crn?community=true",
        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
      ),
    )
    val result = pniService.getPniCalculation(crn)
    assertThat(result).isEqualTo(PniScore.empty())
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.MISSING_INFORMATION)
  }

  @Test
  fun `when getPniScore throws exception returns empty pniScore`() {
    val crn = randomCrn()
    `when`(oasysApiClient.getPniCalculation(crn)).thenThrow(RuntimeException("Network error"))
    val result = pniService.getPniCalculation(crn)
    assertThat(result).isEqualTo(PniScore.empty())
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.MISSING_INFORMATION)
  }

  @Test
  fun `getDailyPniCalculation returns PniScore when OASys has data`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    val result = pniService.getDailyPniCalculation(crn)
    assertThat(result).isNotNull
    assertThat(result!!.overallIntensity).isEqualTo(Type.toIntensity(pniResponse.pniCalculation?.pni))
  }

  @Test
  fun `getDailyPniCalculation returns null when OASys returns 404`() {
    val crn = randomCrn()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Failure.StatusCode(
        HttpMethod.GET,
        "/assessments/pni/$crn?community=true",
        HttpStatus.NOT_FOUND,
        "",
      ),
    )
    val result = pniService.getDailyPniCalculation(crn)
    assertThat(result).isNull()
  }

  @Test
  fun `getDailyPniCalculation returns null when OASys returns 503`() {
    val crn = randomCrn()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Failure.StatusCode(
        HttpMethod.GET,
        "/assessments/pni/$crn?community=true",
        HttpStatus.SERVICE_UNAVAILABLE,
        "",
      ),
    )
    val result = pniService.getDailyPniCalculation(crn)
    assertThat(result).isNull()
  }

  @Test
  fun `getDailyPniCalculation returns null when OASys throws exception`() {
    val crn = randomCrn()
    `when`(oasysApiClient.getPniCalculation(crn)).thenThrow(RuntimeException("Network error"))
    val result = pniService.getDailyPniCalculation(crn)
    assertThat(result).isNull()
  }
}
