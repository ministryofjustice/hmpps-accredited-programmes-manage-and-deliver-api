package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.GovUkApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ScheduleSessionRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.Clock
import java.util.UUID

class ScheduleServiceTest {
  private val programmeGroupRepository = mockk<ProgrammeGroupRepository>()
  private val moduleRepository = mockk<ModuleRepository>()
  private val clock = mockk<Clock>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()
  private val moduleSessionTemplateRepository = mockk<ModuleSessionTemplateRepository>()
  private val govUkApiClient = mockk<GovUkApiClient>()
  private val nDeliusIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private val nDeliusAppointmentRepository = mockk<NDeliusAppointmentRepository>()
  private val facilitatorService = mockk<FacilitatorService>()
  private val referralRepository = mockk<ReferralRepository>()
  private val sessionRepository = mockk<SessionRepository>()
  private lateinit var service: ScheduleService

  @BeforeEach
  fun setup() {
    service = ScheduleService(
      programmeGroupRepository, moduleRepository, clock, programmeGroupMembershipRepository,
      moduleSessionTemplateRepository, govUkApiClient, nDeliusIntegrationApiClient, nDeliusAppointmentRepository,
      facilitatorService, referralRepository, sessionRepository,
    )
  }

  @Test
  fun `should return one to one catchup schedule session message`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionTemplateId = UUID.randomUUID()
    val request = ScheduleSessionRequestFactory().withSessionTemplateId(sessionTemplateId).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Module 1")
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) } returns moduleSessionTemplateEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { sessionRepository.save(any()) } returns sessionEntity
    every {
      nDeliusIntegrationApiClient.createAppointmentsInDelius(any())
    } returns ClientResult.Success(
      status = HttpStatus.OK,
      body = Unit,
    )

    every { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) } returns emptyList()

    // When
    val result = service.scheduleIndividualSession(groupId, request)

    // Then
    assertThat(result.message).isEqualTo("Getting started one-to-one catch-up for John Smith has been added.")

    verify { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) }
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { sessionRepository.save(any()) }
    verify { nDeliusIntegrationApiClient.createAppointmentsInDelius(any()) }
    verify { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) }
  }

  @Test
  fun `should return one to one schedule session message`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionTemplateId = UUID.randomUUID()
    val request = ScheduleSessionRequestFactory().withSessionTemplateId(sessionTemplateId).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Module 1")
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(false)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) } returns moduleSessionTemplateEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { sessionRepository.save(any()) } returns sessionEntity
    every {
      nDeliusIntegrationApiClient.createAppointmentsInDelius(any())
    } returns ClientResult.Success(
      status = HttpStatus.OK,
      body = Unit,
    )

    every { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) } returns emptyList()

    // When
    val result = service.scheduleIndividualSession(groupId, request)

    // Then
    assertThat(result.message).isEqualTo("Getting started one-to-one for John Smith has been added.")

    verify { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) }
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { sessionRepository.save(any()) }
    verify { nDeliusIntegrationApiClient.createAppointmentsInDelius(any()) }
    verify { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) }
  }

  @Test
  fun `should return group catch up schedule session message`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionTemplateId = UUID.randomUUID()
    val request = ScheduleSessionRequestFactory().withSessionTemplateId(sessionTemplateId).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withName("Module 1")
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) } returns moduleSessionTemplateEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { sessionRepository.save(any()) } returns sessionEntity
    every {
      nDeliusIntegrationApiClient.createAppointmentsInDelius(any())
    } returns ClientResult.Success(
      status = HttpStatus.OK,
      body = Unit,
    )

    every { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) } returns emptyList()

    // When
    val result = service.scheduleIndividualSession(groupId, request)

    // Then
    assertThat(result.message).isEqualTo("Getting started 1 catch-up has been added.")

    verify { moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId) }
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { sessionRepository.save(any()) }
    verify { nDeliusIntegrationApiClient.createAppointmentsInDelius(any()) }
    verify { nDeliusAppointmentRepository.saveAll(any<List<NDeliusAppointmentEntity>>()) }
  }
}
