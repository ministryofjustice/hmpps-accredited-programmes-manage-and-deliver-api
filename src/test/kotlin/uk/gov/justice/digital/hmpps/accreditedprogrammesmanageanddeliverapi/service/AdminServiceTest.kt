package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.UUID

class AdminServiceTest {
  private val referralRepository: ReferralRepository = mockk()
  private val referralService: ReferralService = mockk()
  private val sentenceService: SentenceService = mockk()
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()
  private val pniService: PniService = mockk()
  private val nDeliusAppointmentRepository: NDeliusAppointmentRepository = mockk()
  private val transactionTemplate: TransactionTemplate = mockk()
  private val messageHistoryRepository: MessageHistoryRepository = mockk()
  private val telemetryService: TelemetryService = mockk()

  private lateinit var adminService: AdminService

  @BeforeEach
  fun beforeEach() {
    adminService = AdminService(
      referralRepository = referralRepository,
      referralService = referralService,
      sentenceService = sentenceService,
      nDeliusIntegrationApiClient = nDeliusIntegrationApiClient,
      pniService = pniService,
      nDeliusAppointmentRepository = nDeliusAppointmentRepository,
      transactionTemplate = transactionTemplate,
      messageHistoryRepository = messageHistoryRepository,
      telemetryService = telemetryService,
    )
  }

  @Test
  fun `refreshPersonalDetailsForReferralsWithMissingStructuredNames should refresh provided referral IDs`() = runBlocking {
    // Given
    val referralId1 = UUID.randomUUID()
    val referralId2 = UUID.randomUUID()
    val referralIds = listOf(referralId1, referralId2)
    val referral1 = ReferralDetailsFactory().withId(referralId1).produce()
    val referral2 = ReferralDetailsFactory().withId(referralId2).produce()

    coEvery { referralService.refreshPersonalDetailsForReferral(referralId1) } returns referral1
    coEvery { referralService.refreshPersonalDetailsForReferral(referralId2) } returns referral2

    // When
    val result = adminService.refreshPersonalDetailsForReferralsWithMissingStructuredNames(referralIds)

    // Then
    assertThat(result.successIds).containsExactlyInAnyOrder(referralId1, referralId2)
    assertThat(result.notFoundIds).isEmpty()
    assertThat(result.failureIds).isEmpty()

    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(referralId1) }
    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(referralId2) }
    coVerify(exactly = 0) { referralRepository.findAllIdsWherePersonSurnameIsNull() }
  }

  @Test
  fun `refreshPersonalDetailsForReferralsWithMissingStructuredNames should fetch IDs from repository if list is empty`() = runBlocking {
    // Given
    val referralId1 = UUID.randomUUID()
    val referralId2 = UUID.randomUUID()
    val fetchedIds = listOf(referralId1, referralId2)
    val referral1 = ReferralDetailsFactory().withId(referralId1).produce()
    val referral2 = ReferralDetailsFactory().withId(referralId2).produce()

    every { referralRepository.findAllIdsWherePersonSurnameIsNull() } returns fetchedIds
    coEvery { referralService.refreshPersonalDetailsForReferral(referralId1) } returns referral1
    coEvery { referralService.refreshPersonalDetailsForReferral(referralId2) } returns referral2

    // When
    val result = adminService.refreshPersonalDetailsForReferralsWithMissingStructuredNames(emptyList())

    // Then
    assertThat(result.successIds).containsExactlyInAnyOrder(referralId1, referralId2)
    assertThat(result.notFoundIds).isEmpty()
    assertThat(result.failureIds).isEmpty()

    verify(exactly = 1) { referralRepository.findAllIdsWherePersonSurnameIsNull() }
    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(referralId1) }
    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(referralId2) }
  }

  @Test
  fun `refreshPersonalDetailsForReferralsWithMissingStructuredNames should handle mixed results`() = runBlocking {
    // Given
    val successId = UUID.randomUUID()
    val notFoundId = UUID.randomUUID()
    val failureId = UUID.randomUUID()
    val referralIds = listOf(successId, notFoundId, failureId)
    val successReferral = ReferralDetailsFactory().withId(successId).produce()

    coEvery { referralService.refreshPersonalDetailsForReferral(successId) } returns successReferral
    coEvery { referralService.refreshPersonalDetailsForReferral(notFoundId) } returns null
    coEvery { referralService.refreshPersonalDetailsForReferral(failureId) } throws RuntimeException("Something went wrong")

    // When
    val result = adminService.refreshPersonalDetailsForReferralsWithMissingStructuredNames(referralIds)

    // Then
    assertThat(result.successIds).containsExactly(successId)
    assertThat(result.notFoundIds).containsExactly(notFoundId)
    assertThat(result.failureIds).containsExactly(failureId)

    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(successId) }
    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(notFoundId) }
    coVerify(exactly = 1) { referralService.refreshPersonalDetailsForReferral(failureId) }
  }
}
