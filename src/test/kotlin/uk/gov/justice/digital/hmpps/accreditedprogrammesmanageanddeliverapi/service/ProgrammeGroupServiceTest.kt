package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.util.UUID

class ProgrammeGroupServiceTest {
  private val programmeGroupRepository = mockk<ProgrammeGroupRepository>()
  private val groupWaitlistItemViewRepository = mockk<GroupWaitlistItemViewRepository>()
  private val referralReportingLocationRepository = mockk<ReferralReportingLocationRepository>()
  private val userService = mockk<UserService>()
  private val accreditedProgrammeTemplateRepository = mockk<AccreditedProgrammeTemplateRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val sessionRepository = mockk<SessionRepository>()
  private val facilitatorService = mockk<FacilitatorService>()
  private val sessionNameFormatter = mockk<SessionNameFormatter>()
  private val sessionService = mockk<SessionService>()
  private val moduleSessionTemplateRepository = mockk<ModuleSessionTemplateRepository>()
  private lateinit var service: ProgrammeGroupService

  @BeforeEach
  fun setup() {
    service = ProgrammeGroupService(
      programmeGroupRepository,
      groupWaitlistItemViewRepository,
      referralReportingLocationRepository,
      userService,
      accreditedProgrammeTemplateRepository,
      scheduleService,
      sessionRepository,
      facilitatorService,
      sessionNameFormatter,
      sessionService,
      moduleSessionTemplateRepository,
    )
  }

  @Test
  fun shouldThrowExceptionWhenUsernameIsWithoutRegionOnGetGroupWaitlistDataByCriteria() {
    // Given
    val groupId = UUID.randomUUID()
    val username = "user1"
    val pageRequest = PageRequest.of(0, 10)
    val groupEntity = ProgrammeGroupFactory().produce()
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns groupEntity
    every { userService.getUserRegions(username) } returns emptyList()

    // When
    val exception = assertThrows<NotFoundException> {
      service.getGroupWaitlistDataByCriteria(
        pageRequest,
        GroupPageTab.ALLOCATED, groupId, null, null, null, null, null, username,
      )
    }

    // Then
    assertTrue(
      exception.message!!
        .contains("Region for username $username not found"),
    )
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { userService.getUserRegions(username) }
  }
}
