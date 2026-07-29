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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniCalculationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

@ExtendWith(MockitoExtension::class)
class PniServiceTest {

  @Mock
  private lateinit var oasysApiClient: OasysApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

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

  @Test
  fun `displayIneligibleWarning is true when sourcedFrom is REQUIREMENT and overallIntensity is ALTERNATIVE_PATHWAY`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().withPniCalculation(
      PniCalculationFactory().withPni(Type.A).produce(),
    ).produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    val referral = ReferralEntityFactory().withCrn(crn).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()
    `when`(referralRepository.findByCrn(crn)).thenReturn(listOf(referral))

    val result = pniService.getPniCalculation(crn)

    assertThat(result.displayIneligibleWarning).isTrue()
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.ALTERNATIVE_PATHWAY)
  }

  @Test
  fun `displayIneligibleWarning is false when sourcedFrom is REQUIREMENT but overallIntensity is not ALTERNATIVE_PATHWAY`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().withPniCalculation(
      PniCalculationFactory().withPni(Type.H).produce(),
    ).produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    val referral = ReferralEntityFactory().withCrn(crn).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()
    `when`(referralRepository.findByCrn(crn)).thenReturn(listOf(referral))

    val result = pniService.getPniCalculation(crn)

    assertThat(result.displayIneligibleWarning).isFalse()
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.HIGH)
  }

  @Test
  fun `displayIneligibleWarning is false when overallIntensity is ALTERNATIVE_PATHWAY but sourcedFrom is not REQUIREMENT`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().withPniCalculation(
      PniCalculationFactory().withPni(Type.A).produce(),
    ).produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    val referral = ReferralEntityFactory().withCrn(crn).withSourcedFrom(null).produce()
    `when`(referralRepository.findByCrn(crn)).thenReturn(listOf(referral))

    val result = pniService.getPniCalculation(crn)

    assertThat(result.displayIneligibleWarning).isFalse()
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.ALTERNATIVE_PATHWAY)
  }

  @Test
  fun `displayIneligibleWarning is false when neither condition is met`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().withPniCalculation(
      PniCalculationFactory().withPni(Type.M).produce(),
    ).produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    val referral = ReferralEntityFactory().withCrn(crn).withSourcedFrom(null).produce()
    `when`(referralRepository.findByCrn(crn)).thenReturn(listOf(referral))

    val result = pniService.getPniCalculation(crn)

    assertThat(result.displayIneligibleWarning).isFalse()
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.MODERATE)
  }

  @Test
  fun `displayIneligibleWarning is false when no referral exists`() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().withPniCalculation(
      PniCalculationFactory().withPni(Type.A).produce(),
    ).produce()
    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(status = HttpStatus.OK, body = pniResponse),
    )
    `when`(referralRepository.findByCrn(crn)).thenReturn(emptyList())

    val result = pniService.getPniCalculation(crn)

    assertThat(result.displayIneligibleWarning).isFalse()
    assertThat(result.overallIntensity).isEqualTo(OverallIntensity.ALTERNATIVE_PATHWAY)
  }
}
