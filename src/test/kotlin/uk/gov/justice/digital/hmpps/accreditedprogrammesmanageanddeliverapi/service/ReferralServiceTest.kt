package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import java.util.*

@ExtendWith(MockitoExtension::class)
class ReferralServiceTest {

  @Mock
  private lateinit var findAndReferInterventionApiClient: FindAndReferInterventionApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @InjectMocks
  private lateinit var referralService: ReferralService

  @Test
  fun `getReferralDetails should return referral details when present`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralDetails = ReferralDetailsFactory().withReferralId(referralId).produce()

    `when`(findAndReferInterventionApiClient.getReferral(referralId)).thenReturn(
      ClientResult.Success(
        status = HttpStatusCode.valueOf(200),
        body = referralDetails,
      ),
    )

    // When
    val result = referralService.getReferralDetails(referralId)

    // Then
    assertThat(referralDetails).isEqualTo(result)
    verify(findAndReferInterventionApiClient).getReferral(referralId)
  }

  @Test
  fun `getReferralDetails should throw NotFoundException when referral is not found`() {
    // Given
    val referralId = UUID.randomUUID()
    `when`(findAndReferInterventionApiClient.getReferral(referralId)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/referral/$referralId", HttpStatusCode.valueOf(404), ""),
    )

    // When & Then
    assertThrows<NotFoundException> { referralService.getReferralDetails(referralId) }
    verify(findAndReferInterventionApiClient).getReferral(referralId)
  }

  @Test
  fun `createReferral should save referral and add status history`() {
    // Given
    val referralDetails = ReferralDetailsFactory().produce()
    val referralEntity = referralDetails.toReferralEntity() // Use actual transformation
    val statusHistoryEntity = ReferralStatusHistoryEntityFactory().withStatus("Created").produce()

    `when`(referralRepository.save(any())).thenReturn(referralEntity)
    `when`(referralStatusHistoryRepository.save(any())).thenReturn(statusHistoryEntity)

    // When
    referralService.createReferral(referralDetails)

    // Then
    verify(referralRepository).save(
      argThat { saved ->
        saved.crn == referralEntity.crn &&
          saved.interventionType == referralEntity.interventionType &&
          saved.interventionName == referralEntity.interventionName
      },
    )
    verify(referralStatusHistoryRepository).save(any())
  }
}
