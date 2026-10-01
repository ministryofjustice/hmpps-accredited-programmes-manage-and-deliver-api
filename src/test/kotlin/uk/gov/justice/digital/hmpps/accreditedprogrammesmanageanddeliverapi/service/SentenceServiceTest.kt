package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import java.time.LocalDate

class SentenceServiceTest {
  private val nDeliusIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private val telemetryService = mockk<TelemetryService>(relaxed = true)
  private val sentenceService = SentenceService(nDeliusIntegrationApiClient, telemetryService)

  private val crn = "X123456"
  private val eventNumber = 1

  private fun stubSentence(response: uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse) {
    every { nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber) } returns ClientResult.Success(
      HttpStatus.OK,
      response,
    )
  }

  @Test
  fun `getSentenceEndDateDetails returns both expectedEndDate and licenceExpiryDate for a licence condition`() {
    // Given
    val licenceExpiryDate = LocalDate.of(2030, 1, 1)
    val expectedEndDate = LocalDate.of(2029, 1, 1)
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withLicenceExpiryDate(licenceExpiryDate)
        .withExpectedEndDate(expectedEndDate)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.LICENCE_CONDITION)

    // Then
    assertThat(result?.expectedEndDate).isEqualTo(expectedEndDate)
    assertThat(result?.licenceExpiryDate).isEqualTo(licenceExpiryDate)
  }

  @Test
  fun `getSentenceEndDateDetails returns null licenceExpiryDate for a licence condition when licenceExpiryDate is null`() {
    // Given
    val expectedEndDate = LocalDate.of(2029, 1, 1)
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withLicenceExpiryDate(null)
        .withExpectedEndDate(expectedEndDate)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.LICENCE_CONDITION)

    // Then
    assertThat(result?.expectedEndDate).isEqualTo(expectedEndDate)
    assertThat(result?.licenceExpiryDate).isNull()
  }

  @Test
  fun `getSentenceEndDateDetails returns null dates for a licence condition when both licenceExpiryDate and expectedEndDate are null`() {
    // Given
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withLicenceExpiryDate(null)
        .withExpectedEndDate(null)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.LICENCE_CONDITION)

    // Then
    assertThat(result?.expectedEndDate).isNull()
    assertThat(result?.licenceExpiryDate).isNull()
  }

  @Test
  fun `getSentenceEndDateDetails returns expectedEndDate and nulls licenceExpiryDate for a requirement`() {
    // Given
    val licenceExpiryDate = LocalDate.of(2030, 1, 1)
    val expectedEndDate = LocalDate.of(2029, 1, 1)
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withLicenceExpiryDate(licenceExpiryDate)
        .withExpectedEndDate(expectedEndDate)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.REQUIREMENT)

    // Then
    assertThat(result?.expectedEndDate).isEqualTo(expectedEndDate)
    assertThat(result?.licenceExpiryDate).isNull()
  }

  @Test
  fun `getSentenceEndDateDetails returns null expectedEndDate for a requirement when expectedEndDate is null`() {
    // Given
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withExpectedEndDate(null)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.REQUIREMENT)

    // Then
    assertThat(result?.expectedEndDate).isNull()
    assertThat(result?.licenceExpiryDate).isNull()
  }

  @Test
  fun `getSentenceEndDateDetails nulls licenceExpiryDate when sentence type is null`() {
    // Given
    val expectedEndDate = LocalDate.of(2029, 1, 1)
    stubSentence(
      NDeliusSentenceResponseFactory()
        .withLicenceExpiryDate(LocalDate.of(2030, 1, 1))
        .withExpectedEndDate(expectedEndDate)
        .produce(),
    )

    // When
    val result = sentenceService.getSentenceEndDateDetails(crn, eventNumber, null)

    // Then
    assertThat(result?.expectedEndDate).isEqualTo(expectedEndDate)
    assertThat(result?.licenceExpiryDate).isNull()
  }

  @Test
  fun `getSentenceEndDateDetails throws NotFoundException when no sentence information is found`() {
    // Given
    every { nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/sentence",
      HttpStatus.NOT_FOUND,
      "Not found",
    )

    // When / Then
    assertThatThrownBy {
      sentenceService.getSentenceEndDateDetails(crn, eventNumber, ReferralEntitySourcedFrom.LICENCE_CONDITION)
    }.isInstanceOf(NotFoundException::class.java)
  }
}
