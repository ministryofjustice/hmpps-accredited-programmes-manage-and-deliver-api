package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toAppointment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toNdeliusAppointmentEntity
import java.util.UUID

/**
 * Unit tests for the terminated-requirement telemetry branch added inside
 * `ScheduleService.createNdeliusAppointmentsForSessions`.
 *
 * `AttendeeEntity.toAppointment` and `AttendeeEntity.toNdeliusAppointmentEntity` read
 * a deep object graph (session -> programmeGroup -> treatmentManager, moduleSessionTemplate,
 * etc.). Rather than build all that up we `mockkStatic` the two extensions so the test
 * focuses purely on the failure-handling branch we own.
 */
class ScheduleServiceTest {

  private val nDeliusIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private val telemetryClient = mockk<TelemetryClient>(relaxed = true)

  private val scheduleService = ScheduleService(
    programmeGroupRepository = mockk(relaxed = true),
    moduleRepository = mockk(relaxed = true),
    clock = mockk(relaxed = true),
    programmeGroupMembershipRepository = mockk(relaxed = true),
    moduleSessionTemplateRepository = mockk(relaxed = true),
    govUkApiClient = mockk(relaxed = true),
    nDeliusIntegrationApiClient = nDeliusIntegrationApiClient,
    nDeliusAppointmentRepository = mockk(relaxed = true),
    facilitatorService = mockk(relaxed = true),
    referralRepository = mockk(relaxed = true),
    sessionRepository = mockk(relaxed = true),
    sessionNameFormatter = mockk(relaxed = true),
    bankHolidayRepository = mockk(relaxed = true),
    telemetryClient = telemetryClient,
  )

  @BeforeEach
  fun setup() {
    // Stub the two extension functions used by createNdeliusAppointmentsForSessions so
    // we don't need to build the full session -> programmeGroup -> treatmentManager graph.
    // Function-reference form is refactor-safe (unlike the string "...Kt" form) - renaming
    // the enclosing file will fail at compile time, not silently at runtime.
    mockkStatic(AttendeeEntity::toAppointment, AttendeeEntity::toNdeliusAppointmentEntity)
    every { any<AttendeeEntity>().toAppointment(any()) } returns mockk<CreateAppointmentRequest.NdeliusAppointment>(relaxed = true)
    every { any<AttendeeEntity>().toNdeliusAppointmentEntity(any()) } returns mockk<NDeliusAppointmentEntity>(relaxed = true)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `fires terminated-requirement telemetry and throws BusinessException when nDelius returns a terminated body`() {
    every { nDeliusIntegrationApiClient.createAppointmentsInDelius(any()) } returns
      ClientResult.Failure.StatusCode(
        method = HttpMethod.POST,
        path = "/appointments",
        status = HttpStatusCode.valueOf(400),
        body = """{"status":400,"message":"Invalid Requirement IDs: [1503618208]"}""",
      )

    assertThatThrownBy { scheduleService.createNdeliusAppointmentsForSessions(listOf(buildAttendee())) }
      .isInstanceOf(BusinessException::class.java)

    // Both the generic .failure event and the finer .terminated-requirement event fire.
    verify(exactly = 1) {
      telemetryClient.logToAppInsights("Appointment.create-nDelius.failure", any())
    }
    verify(exactly = 1) {
      telemetryClient.logToAppInsights("Appointment.create-nDelius.terminated-requirement", any())
    }
  }

  @Test
  fun `does not fire terminated-requirement telemetry when the response body does not contain the marker`() {
    every { nDeliusIntegrationApiClient.createAppointmentsInDelius(any()) } returns
      ClientResult.Failure.StatusCode(
        method = HttpMethod.POST,
        path = "/appointments",
        status = HttpStatusCode.valueOf(400),
        body = """{"status":400,"message":"Some other validation failure"}""",
      )

    assertThatThrownBy { scheduleService.createNdeliusAppointmentsForSessions(listOf(buildAttendee())) }
      .isInstanceOf(BusinessException::class.java)

    verify(exactly = 1) {
      telemetryClient.logToAppInsights("Appointment.create-nDelius.failure", any())
    }
    verify(exactly = 0) {
      telemetryClient.logToAppInsights("Appointment.create-nDelius.terminated-requirement", any())
    }
  }

  private fun buildAttendee(
    crn: String = "X123456",
    eventNumber: Int = 1,
    groupId: UUID = UUID.randomUUID(),
  ): AttendeeEntity {
    val referral = mockk<ReferralEntity>(relaxed = true) {
      every { this@mockk.crn } returns crn
      every { this@mockk.eventNumber } returns eventNumber
    }
    val programmeGroup = mockk<ProgrammeGroupEntity>(relaxed = true) {
      every { id } returns groupId
    }
    val session = mockk<SessionEntity>(relaxed = true) {
      every { this@mockk.programmeGroup } returns programmeGroup
    }
    return mockk<AttendeeEntity>(relaxed = true) {
      every { this@mockk.referral } returns referral
      every { this@mockk.session } returns session
    }
  }
}
