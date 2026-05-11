package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

@ExtendWith(MockitoExtension::class)
class ReferralEventNumberResolverServiceTest {

  @Mock
  private lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Test
  fun `does not attempt resolution when event number is already non-zero`() {
    val referral = ReferralEntityFactory().withEventNumber(4).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(4)
    verifyNoInteractions(nDeliusIntegrationApiClient)
    verifyNoInteractions(referralRepository)
    verifyNoInteractions(telemetryClient)
  }

  @Test
  fun `updates referral event number when a valid number is found`() {
    val referral = ReferralEntityFactory().withEventNumber(0).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 1)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/case/${referral.crn}/sentence/1", HttpStatusCode.valueOf(404), null),
    )
    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 2)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/case/${referral.crn}/sentence/2", HttpStatusCode.valueOf(404), null),
    )
    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 3)).thenReturn(
      ClientResult.Success(HttpStatusCode.valueOf(200), mockSentenceResponse()),
    )

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(3)
    assertThat(referral.eventNumber).isEqualTo(3)
    verify(referralRepository).save(referral)
    verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, 1)
    verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, 2)
    verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, 3)
  }

  @Test
  fun `keeps event number as zero when no valid number is found`() {
    val referral = ReferralEntityFactory().withEventNumber(0).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    (1..20).forEach { candidate ->
      `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, candidate)).thenReturn(
        ClientResult.Failure.StatusCode(HttpMethod.GET, "/case/${referral.crn}/sentence/$candidate", HttpStatusCode.valueOf(404), null),
      )
    }

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(0)
    assertThat(referral.eventNumber).isEqualTo(0)
    verify(referralRepository, never()).save(referral)
    (1..20).forEach { candidate ->
      verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, candidate)
    }
  }

  @Test
  fun `continues to next candidate when an unexpected error occurs`() {
    val referral = ReferralEntityFactory().withEventNumber(0).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 1)).thenReturn(
      ClientResult.Failure.Other(
        HttpMethod.GET,
        "/case/${referral.crn}/sentence/1",
        RuntimeException("Connection refused"),
        "nDelius",
      ),
    )
    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 2)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/case/${referral.crn}/sentence/2", HttpStatusCode.valueOf(400), null),
    )

    `when`(nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, 3)).thenReturn(
      ClientResult.Success(HttpStatusCode.valueOf(400), mockSentenceResponse()),
    )

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(3)
    assertThat(referral.eventNumber).isEqualTo(3)
    verify(referralRepository).save(referral)
    verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, 1)
    verify(nDeliusIntegrationApiClient).getSentenceInformation(referral.crn, 2)
  }

  private fun mockSentenceResponse() = NDeliusSentenceResponseFactory().produce()
}
