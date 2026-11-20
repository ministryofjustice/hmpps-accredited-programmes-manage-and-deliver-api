package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
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
  inner class UpdateNameByFacilitatorIds {

    @Test
    fun `should update personal names for provided facilitator IDs successfully`() {
      runBlocking {
        // Given
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val facilitator1 = FacilitatorEntityFactory()
          .withId(id1)
          .withNdeliusPersonCode("CODE1")
          .produce()
        val facilitator2 = FacilitatorEntityFactory()
          .withId(id2)
          .withNdeliusPersonCode("CODE2")
          .produce()

        val staffDto1 = StaffDtoFactory()
          .withForename("Alice")
          .withMiddleNames("Marie")
          .withSurname("Smith")
          .produce()
        val staffDto2 = StaffDtoFactory()
          .withForename("Bob")
          .withMiddleNames(null)
          .withSurname("Jones")
          .produce()

        every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
        every { facilitatorRepository.findById(id2) } returns Optional.of(facilitator2)
        every { nDeliusIntegrationApiClient.getStaffByCode("CODE1") } returns ClientResult.Success(
          HttpStatus.OK,
          staffDto1,
        )
        every { nDeliusIntegrationApiClient.getStaffByCode("CODE2") } returns ClientResult.Success(
          HttpStatus.OK,
          staffDto2,
        )
        every { facilitatorRepository.save(facilitator1) } returns facilitator1
        every { facilitatorRepository.save(facilitator2) } returns facilitator2

        // When
        val result = facilitatorService.updateNameByFacilitatorIds(listOf(id1, id2))

        // Then
        assertThat(result.successIds).containsExactly(id1, id2)
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).isEmpty()

        assertThat(facilitator1.personForename).isEqualTo("Alice")
        assertThat(facilitator1.personMiddleNames).isEqualTo("Marie")
        assertThat(facilitator1.personSurname).isEqualTo("Smith")

        assertThat(facilitator2.personForename).isEqualTo("Bob")
        assertThat(facilitator2.personMiddleNames).isNull()
        assertThat(facilitator2.personSurname).isEqualTo("Jones")

        verify(exactly = 1) { facilitatorRepository.findById(id1) }
        verify(exactly = 1) { facilitatorRepository.findById(id2) }
        verify(exactly = 1) { nDeliusIntegrationApiClient.getStaffByCode("CODE1") }
        verify(exactly = 1) { nDeliusIntegrationApiClient.getStaffByCode("CODE2") }
        verify(exactly = 1) { facilitatorRepository.save(facilitator1) }
        verify(exactly = 1) { facilitatorRepository.save(facilitator2) }
        verify(exactly = 0) { facilitatorRepository.findAllWherePersonSurnameIsNull(any()) }

        verify(exactly = 2) {
          telemetryService.logToAppInsights(
            eventName = "${GET_STAFF_N_DELIUS.eventName}.success",
            integrationActionType = GET_STAFF_N_DELIUS.name,
            outcome = "success",
          )
        }
      }
    }

    @Test
    fun `should fetch facilitator IDs where surname is null when facilitatorIds is empty`() {
      runBlocking {
        // Given
        val id1 = UUID.randomUUID()
        val facilitator1 = FacilitatorEntityFactory()
          .withId(id1)
          .withNdeliusPersonCode("CODE1")
          .produce()
        val staffDto1 = StaffDtoFactory().produce()

        // every { facilitatorRepository.findAllWherePersonSurnameIsNull(any()) } returns listOf(id1)
        every { facilitatorRepository.findById(id1) } returns Optional.of(facilitator1)
        every { nDeliusIntegrationApiClient.getStaffByCode(facilitator1.ndeliusPersonCode) } returns ClientResult.Success(
          HttpStatus.OK,
          staffDto1,
        )
        every { facilitatorRepository.save(facilitator1) } returns facilitator1

        // When
        val result = facilitatorService.updateNameByFacilitatorIds(emptyList())

        // Then
        assertThat(result.successIds).containsExactly(id1)
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).isEmpty()

        verify(exactly = 1) { facilitatorRepository.findAllWherePersonSurnameIsNull(any()) }
        verify(exactly = 1) { facilitatorRepository.findById(id1) }
        verify(exactly = 1) { facilitatorRepository.save(facilitator1) }
      }
    }

    @Test
    fun `should record failure when upstream client call fails with StatusCode`() {
      runBlocking {
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
        val result = facilitatorService.updateNameByFacilitatorIds(listOf(id1))

        // Then
        assertThat(result.successIds).isEmpty()
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).containsExactly(id1)

        verify(exactly = 1) {
          telemetryService.logToAppInsights(
            eventName = "${GET_STAFF_N_DELIUS.eventName}.failure",
            integrationActionType = GET_STAFF_N_DELIUS.name,
            outcome = "failure",
          )
        }
        verify(exactly = 0) { facilitatorRepository.save(any()) }
      }
    }

    @Test
    fun `should record failure when upstream client call fails with Other exception`() {
      runBlocking {
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
        val result = facilitatorService.updateNameByFacilitatorIds(listOf(id1))

        // Then
        assertThat(result.successIds).isEmpty()
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).containsExactly(id1)

        verify(exactly = 1) {
          telemetryService.logToAppInsights(
            eventName = "${GET_STAFF_N_DELIUS.eventName}.failure",
            integrationActionType = GET_STAFF_N_DELIUS.name,
            outcome = "failure",
          )
        }
        verify(exactly = 0) { facilitatorRepository.save(any()) }
      }
    }

    @Test
    fun `should record failure when repository save throws exception`() {
      runBlocking {
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

        // When
        val result = facilitatorService.updateNameByFacilitatorIds(listOf(id1))

        // Then
        assertThat(result.successIds).isEmpty()
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).containsExactly(id1)
      }
    }

    @Test
    fun `should handle mixed outcomes across multiple facilitators`() {
      runBlocking {
        // Given
        val successId = UUID.randomUUID()
        val failIdClient = UUID.randomUUID()
        val failIdDb = UUID.randomUUID()

        val successFacilitator =
          FacilitatorEntityFactory().withId(successId).withNdeliusPersonCode("CODE_OK").produce()
        val failFacilitatorClient =
          FacilitatorEntityFactory().withId(failIdClient).withNdeliusPersonCode("CODE_FAIL").produce()
        val failFacilitatorDb = FacilitatorEntityFactory().withId(failIdDb).withNdeliusPersonCode("CODE_DB").produce()

        val staffDtoSuccess = StaffDtoFactory().produce()
        val staffDtoDb = StaffDtoFactory().produce()

        every { facilitatorRepository.findById(successId) } returns Optional.of(successFacilitator)
        every { facilitatorRepository.findById(failIdClient) } returns Optional.of(failFacilitatorClient)
        every { facilitatorRepository.findById(failIdDb) } returns Optional.of(failFacilitatorDb)

        every { nDeliusIntegrationApiClient.getStaffByCode("CODE_OK") } returns ClientResult.Success(
          HttpStatus.OK,
          staffDtoSuccess,
        )
        every { nDeliusIntegrationApiClient.getStaffByCode("CODE_FAIL") } returns ClientResult.Failure.Other(
          method = HttpMethod.GET,
          path = "/staff/CODE_FAIL",
          exception = RuntimeException("Network error"),
          serviceName = "n-delius",
        )
        every { nDeliusIntegrationApiClient.getStaffByCode("CODE_DB") } returns ClientResult.Success(
          HttpStatus.OK,
          staffDtoDb,
        )

        every { facilitatorRepository.save(successFacilitator) } returns successFacilitator
        every { facilitatorRepository.save(failFacilitatorDb) } throws RuntimeException("DB save failed")

        // When
        val result = facilitatorService.updateNameByFacilitatorIds(listOf(successId, failIdClient, failIdDb))

        // Then
        assertThat(result.successIds).containsExactly(successId)
        assertThat(result.notFoundIds).isEmpty()
        assertThat(result.failureIds).containsExactly(failIdClient, failIdDb)

        verify(exactly = 1) { facilitatorRepository.save(successFacilitator) }
      }
    }
  }
}
