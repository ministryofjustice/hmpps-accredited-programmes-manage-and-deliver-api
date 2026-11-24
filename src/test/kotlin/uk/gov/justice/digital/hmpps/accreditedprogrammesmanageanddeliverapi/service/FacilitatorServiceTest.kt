package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.StaffDtoFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_STAFF_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import java.util.Optional
import java.util.UUID

class FacilitatorServiceTest {
  private val facilitatorRepository: FacilitatorRepository = mockk()
  private val telemetryService: TelemetryService = mockk(relaxed = true)
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()

  private lateinit var facilitatorService: FacilitatorService

  @BeforeEach
  fun setUp() {
    facilitatorService = FacilitatorService(
      facilitatorRepository = facilitatorRepository,
      telemetryService = telemetryService,
      nDeliusIntegrationApiClient = nDeliusIntegrationApiClient,
    )
  }

  @Nested
  inner class UpdateNameByFacilitatorId {

    @Test
    fun `should update personal names for provided facilitator IDs successfully`() {
      // Given
      val id1 = UUID.randomUUID()
      val facilitator1 = FacilitatorEntityFactory()
        .withId(id1)
        .withNdeliusPersonCode("CODE1")
        .produce()

      val staffDto1 = StaffDtoFactory()
        .withForename("Alice")
        .withMiddleNames("Marie")
        .withSurname("Smith")
        .produce()
      every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
      every { nDeliusIntegrationApiClient.getStaffByCode("CODE1") } returns ClientResult.Success(
        HttpStatus.OK,
        staffDto1,
      )
      every { facilitatorRepository.save(facilitator1) } returns facilitator1

      runBlocking {
        // When
        val result = facilitatorService.updateNameByFacilitatorId(id1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(id1)
        assertThat(result?.personForename).isEqualTo("Alice")
        assertThat(result?.personMiddleNames).isEqualTo("Marie")
        assertThat(result?.personSurname).isEqualTo("Smith")

        verify(exactly = 1) { facilitatorRepository.findById(id1) }
        verify(exactly = 1) { nDeliusIntegrationApiClient.getStaffByCode("CODE1") }
        verify(exactly = 1) { facilitatorRepository.save(facilitator1) }
        verify(exactly = 0) { facilitatorRepository.findAllWherePersonSurnameIsNull(any()) }

        verify(exactly = 1) {
          telemetryService.logToAppInsights(
            eventName = "${GET_STAFF_N_DELIUS.eventName}.success",
            integrationActionType = GET_STAFF_N_DELIUS.name,
            outcome = "success",
          )
        }
      }
    }

    @Test
    fun `should return null when facilitator is not found`() {
      // Given
      val id = UUID.randomUUID()
      every { facilitatorRepository.findById(id) } returns Optional.empty()

      runBlocking {
        // When
        val result = facilitatorService.updateNameByFacilitatorId(id)

        // Then
        assertThat(result).isNull()
        verify(exactly = 0) { nDeliusIntegrationApiClient.getStaffByCode(any()) }
        verify(exactly = 0) { facilitatorRepository.save(any()) }
      }
    }

    @Test
    fun `should throw and log failure when upstream client call fails with StatusCode`() {
      // Given
      val id1 = UUID.randomUUID()
      val facilitator1 = FacilitatorEntityFactory()
        .withId(id1)
        .withNdeliusPersonCode("CODE1")
        .produce()

      every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
      every { nDeliusIntegrationApiClient.getStaffByCode("CODE1") } returns ClientResult.Failure.StatusCode(
        method = HttpMethod.GET,
        path = "/staff/CODE1",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        body = "Internal Server Error",
      )

      // When
      runBlocking {
        assertThatThrownBy { runBlocking { facilitatorService.updateNameByFacilitatorId(id1) } }
          .isInstanceOf(BusinessException::class.java)
          .hasMessageContaining("CODE1")
      }

      verify(exactly = 1) {
        telemetryService.logToAppInsights(
          eventName = "${GET_STAFF_N_DELIUS.eventName}.failure",
          integrationActionType = GET_STAFF_N_DELIUS.name,
          outcome = "failure",
        )
      }
      verify(exactly = 0) { facilitatorRepository.save(any()) }
    }

    @Test
    fun `should throw and log failure when upstream client call fails with Other exception`() {
      // Given
      val id1 = UUID.randomUUID()
      val facilitator1 = FacilitatorEntityFactory()
        .withId(id1)
        .withNdeliusPersonCode("CODE1")
        .produce()

      every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
      every { nDeliusIntegrationApiClient.getStaffByCode("CODE1") } returns ClientResult.Failure.Other(
        method = HttpMethod.GET,
        path = "/staff/CODE1",
        exception = RuntimeException("Connection timeout"),
        serviceName = "n-delius",
      )

      // When
      runBlocking {
        assertThatThrownBy { runBlocking { facilitatorService.updateNameByFacilitatorId(id1) } }
          .isInstanceOf(BusinessException::class.java)
          .hasMessageContaining("CODE1")
      }

      verify(exactly = 1) {
        telemetryService.logToAppInsights(
          eventName = "${GET_STAFF_N_DELIUS.eventName}.failure",
          integrationActionType = GET_STAFF_N_DELIUS.name,
          outcome = "failure",
        )
      }
      verify(exactly = 0) { facilitatorRepository.save(any()) }
    }

    @Test
    fun `should propagate exception when repository save throws exception`() {
      // Given
      val id1 = UUID.randomUUID()
      val facilitator1 = FacilitatorEntityFactory()
        .withId(id1)
        .withNdeliusPersonCode("CODE1")
        .produce()
      val staffDto = StaffDtoFactory().produce()

      every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
      every { nDeliusIntegrationApiClient.getStaffByCode(facilitator1.ndeliusPersonCode) } returns ClientResult.Success(
        HttpStatus.OK,
        staffDto,
      )
      every { facilitatorRepository.save(facilitator1) } throws RuntimeException("Database error")

      // When/Then
      runBlocking {
        assertThatThrownBy { runBlocking { facilitatorService.updateNameByFacilitatorId(id1) } }
          .isInstanceOf(RuntimeException::class.java)
          .hasMessage("Database error")
      }
    }
  }
}
