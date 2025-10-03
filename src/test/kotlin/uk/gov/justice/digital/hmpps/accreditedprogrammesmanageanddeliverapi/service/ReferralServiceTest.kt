package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReferralServiceTest {

  @Mock
  private lateinit var ndeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Mock
  private lateinit var findAndReferInterventionApiClient: FindAndReferInterventionApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Mock
  private lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Mock
  private lateinit var serviceUserService: ServiceUserService

  @Mock
  private lateinit var ldcService: LdcService

  @Mock
  private lateinit var cohortService: CohortService

  @Mock
  private lateinit var pniService: PniService

  @Mock
  private lateinit var referralLdcHistoryRepository: ReferralLdcHistoryRepository

  @Mock
  private lateinit var referralReportingLocationRepository: ReferralReportingLocationRepository

  @InjectMocks
  private lateinit var referralService: ReferralService

  @Test
  fun `getFindAndReferReferralDetails should return referral details when present`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralDetails = FindAndReferReferralDetailsFactory().withReferralId(referralId).produce()

    `when`(findAndReferInterventionApiClient.getFindAndReferReferral(referralId)).thenReturn(
      ClientResult.Success(
        status = HttpStatusCode.valueOf(200),
        body = referralDetails,
      ),
    )

    // When
    val result = referralService.getFindAndReferReferralDetails(referralId)

    // Then
    assertThat(referralDetails).isEqualTo(result)
    verify(findAndReferInterventionApiClient).getFindAndReferReferral(referralId)
  }

  @Test
  fun `getFindAndReferReferralDetails should throw NotFoundException when referral is not found`() {
    // Given
    val referralId = UUID.randomUUID()
    `when`(findAndReferInterventionApiClient.getFindAndReferReferral(referralId)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/referral/$referralId", HttpStatusCode.valueOf(404), ""),
    )

    // When & Then
    assertThrows<NotFoundException> { referralService.getFindAndReferReferralDetails(referralId) }
    verify(findAndReferInterventionApiClient).getFindAndReferReferral(referralId)
  }
}
