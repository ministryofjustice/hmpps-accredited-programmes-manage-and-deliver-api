package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var service: ProgrammeGroupService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @Autowired
  private lateinit var moduleRepository: ModuleRepository

  @Nested
  @DisplayName("getProgrammeGroupsForRegion")
  inner class GetProgrammeGroupsForRegion {
    val pageable: Pageable = Pageable.ofSize(10)

    @BeforeEach
    fun setup() {
      nDeliusApiStubs.clearAllStubs()
      govUkApiStubs.stubBankHolidaysResponse()

      stubAuthTokenEndpoint()

      // Given
      nDeliusApiStubs.stubUserTeamsResponse(
        "the_username",
        NDeliusUserTeams(
          listOf(
            NDeliusUserTeam(
              code = "the_code",
              "The Team Description",
              pdu = CodeDescription("PDU_CODE", "PDU Description"),
              region = CodeDescription("REGION_CODE", "Region Description"),
            ),
          ),
        ),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("THE_GROUP_CODE")
          .withProbationDeliveryUnit("PDU Description", "PDU_CODE")
          .withDeliveryLocation("Location One", "LOC_ONE")
          .withRegionName("Region Description")
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("GROUP_TWO_CODE")
          .withProbationDeliveryUnit("Another PDU Description", "ANOTHER_PDU_CODE")
          .withDeliveryLocation("Location Two", "LOC_TWO")
          .withRegionName("Region Description")
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("GROUP_THREE_CODE")
          .withProbationDeliveryUnit("PDU Description", "PDU_CODE")
          .withRegionName("Region Description")
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("THE_VERY_FAR_AWAY_GROUP")
          .withRegionName("A very far away Region")
          .produce(),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withCode("THE_FEMALE_GROUP_CODE")
          .withSex(ProgrammeGroupSexEnum.FEMALE)
          .withRegionName("Region Description")
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )
    }

    @AfterEach
    fun tearDown() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `Throwing an error if the User is not part of any teams`() {
      // Given
      nDeliusApiStubs.stubUserTeamsResponse(
        "the_username",
        NDeliusUserTeams(
          emptyList(),
        ),
      )

      // When
      val exception = assertThrows<NotFoundException> {
        service.getProgrammeGroupsForRegion(
          pageable,
          groupCode = null,
          pdu = null,
          deliveryLocations = null,
          cohort = null,
          sex = null,
          selectedTab = GroupPageByRegionTab.NOT_STARTED,
          username = "the_username",
        )
      }

      assertThat(exception).hasMessageContaining("Cannot find any regions (or teams) for user the_username")
    }

    @Test
    fun `Returning the ProgrammeGroups for a Region`() {
      // When
      val programmeGroups = service.getProgrammeGroupsForRegion(
        pageable = Pageable.ofSize(10),
        groupCode = null,
        pdu = null,
        deliveryLocations = null,
        cohort = null,
        sex = "MALE",
        selectedTab = GroupPageByRegionTab.NOT_STARTED,
        username = "the_username",
      )

      // Then
      assertThat(programmeGroups.pagedGroupData.totalElements).isEqualTo(3)
      assertThat(programmeGroups.pagedGroupData.map { it.code }).containsExactlyInAnyOrder(
        "THE_GROUP_CODE",
        "GROUP_TWO_CODE",
        "GROUP_THREE_CODE",
      )
      // Scenario 1: pdu not specified - should return all unique PDU names
      assertThat(programmeGroups.probationDeliveryUnitNames).containsExactlyInAnyOrder(
        "PDU Description",
        "Another PDU Description",
        "Test PDU 1",
      )
      // deliveryLocationNames should be null when pdu is not specified
      assertThat(programmeGroups.deliveryLocationNames).isNull()
    }

    @Test
    fun `Partial matching the group code`() {
      // When
      val programmeGroups = service.getProgrammeGroupsForRegion(
        pageable = Pageable.ofSize(10),
        groupCode = "THE_G",
        pdu = null,
        deliveryLocations = null,
        cohort = null,
        sex = null,
        selectedTab = GroupPageByRegionTab.NOT_STARTED,
        username = "the_username",
      )

      // Then
      assertThat(programmeGroups.pagedGroupData.totalElements).isEqualTo(1)
      assertThat(programmeGroups.pagedGroupData.first().code).isEqualTo("THE_GROUP_CODE")
    }

    @Test
    fun `Returns unique delivery locations when pdu is specified`() {
      // When - filtering by specific PDU
      val programmeGroups = service.getProgrammeGroupsForRegion(
        pageable = Pageable.ofSize(10),
        groupCode = null,
        pdu = "PDU Description",
        deliveryLocations = null,
        cohort = null,
        sex = "MALE",
        selectedTab = GroupPageByRegionTab.NOT_STARTED,
        username = "the_username",
      )

      // Then
      assertThat(programmeGroups.pagedGroupData.totalElements).isEqualTo(2)
      assertThat(programmeGroups.pagedGroupData.map { it.code }).containsExactlyInAnyOrder(
        "THE_GROUP_CODE",
        "GROUP_THREE_CODE",
      )
      assertThat(programmeGroups.probationDeliveryUnitNames).containsExactlyInAnyOrder(
        "PDU Description",
        "Another PDU Description",
        "Test PDU 1",
      )
      assertThat(programmeGroups.deliveryLocationNames).containsExactlyInAnyOrder("Location One", "Delivery Location 1")
    }
  }

  @Nested
  @DisplayName("createGroup")
  inner class CreateGroup {
    @Test
    fun `should schedule the Sessions for a Programme Group`() {
      // Given
      stubAuthTokenEndpoint()
      govUkApiStubs.stubBankHolidaysResponse()
      nDeliusApiStubs.stubUserTeamsResponse(
        "the_username",
        NDeliusUserTeams(
          teams = listOf(
            NDeliusUserTeam(
              code = "TEAM001",
              description = "Test Team 1",
              pdu = CodeDescription("PDU001", "Test PDU 1"),
              region = CodeDescription("REGION001", "WIREMOCKED REGION"),
            ),
          ),
        ),
      )

      val slots = mutableSetOf(
        CreateGroupSessionSlotFactory().produce(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 10,
          minutes = 0,
          amOrPm = AmOrPm.AM,
        ),
        CreateGroupSessionSlotFactory().produce(
          dayOfWeek = DayOfWeek.FRIDAY,
          hour = 5,
          minutes = 30,
          amOrPm = AmOrPm.PM,
        ),
      )

      val teamMembers = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.TREATMENT_MANAGER).produce(),
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      )

      val createProgrammeGroup = CreateGroupRequestFactory().produce(
        groupCode = "THE_GROUPCODE",
        cohort = ProgrammeGroupCohort.GENERAL,
        sex = ProgrammeGroupSexEnum.MALE,
        pduName = "THE_PDU_NAME",
        pduCode = "THE_PDU_CODE",
        deliveryLocationName = "THE_DELIVERYLOCATIONNAME",
        deliveryLocationCode = "THE_DELIVERYLOCATIONCODE",
        teamMembers = teamMembers,
        earliestStartDate = LocalDate.of(2025, 3, 25),
        createGroupSessionSlot = slots,
      )

      // When
      service.createGroup(createProgrammeGroup, "the_username")

      // Then
      val foundGroup = programmeGroupRepository.findByCode("THE_GROUPCODE")!!

      val friday28thAt17h30: LocalDateTime = LocalDateTime
        .of(2025, 3, 28, 17, 30)
        .atZone(ZoneId.of("Europe/London"))
        .toLocalDateTime()

      // 3 week buffer between 1st pre group session and the rest
      // and skip the bank holiday friday and monday slot
      val friday25thAprilAt17h30InBst: LocalDateTime = LocalDateTime
        .of(2025, 4, 25, 17, 30)
        .atZone(ZoneId.of("Europe/London"))
        .toLocalDateTime()

      // Check that when we go over the BST/DST switch our time of session is not changed as we only care about the
      // wall clock time of the session.
      assertThat(
        foundGroup.sessions.find {
          it.startsAt == friday28thAt17h30
        },
      ).isNotNull

      assertThat(
        foundGroup.sessions.find {
          it.startsAt == friday25thAprilAt17h30InBst
        },
      ).isNotNull
    }
  }

  @Nested
  @DisplayName("getScheduleForGroup")
  inner class GetScheduleForGroup {

    private lateinit var buildingChoicesTemplateId: UUID
    private lateinit var preGroupModuleId: UUID
    private lateinit var gettingStartedModuleId: UUID
    private lateinit var regularModuleId: UUID
    private lateinit var groupId: UUID

    @AfterEach
    fun tearDown() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return the group schedule with correctly derived start and end dates`() {
      // Given
      val template = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      assertThat(template).isNotNull
      buildingChoicesTemplateId = template.id!!

      val modules = moduleRepository.findByAccreditedProgrammeTemplateId(buildingChoicesTemplateId)
      assertThat(modules).isNotEmpty

      // Find Pre-Group module
      val preGroupModule = modules.find { it.name.startsWith("Pre-group") }
      assertThat(preGroupModule).isNotNull
      preGroupModuleId = preGroupModule!!.id!!

      // Find Getting Started module
      val gettingStartedModule = modules.find { it.name.startsWith("Getting started") }
      assertThat(gettingStartedModule).isNotNull
      gettingStartedModuleId = gettingStartedModule!!.id!!

      // Find regular last module (module_number = last)
      val regularModule = modules.find { it.moduleNumber == modules.last().moduleNumber }
      assertThat(regularModule).isNotNull
      regularModuleId = regularModule!!.id!!

      val preGroupModuleSessions = moduleSessionTemplateRepository.findByModuleId(preGroupModuleId)

      // Create a test group associated with Building Choices template
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("SCHEDULE_GROUP")
          .withRegionName("Test Region")
          .withAccreditedProgrammeTemplate(template)
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )
      groupId = group.id!!

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(preGroupModuleSessions.first({ it.name.startsWith("Pre-group") }))
          .withStartsAt(LocalDateTime.of(2026, 6, 1, 10, 0))
          .withEndsAt(LocalDateTime.of(2026, 6, 1, 11, 0))
          .withIsPlaceholder(false)
          .produce(),
      )

      val gettingStartedModuleSessions = moduleSessionTemplateRepository.findByModuleId(gettingStartedModuleId)

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(gettingStartedModuleSessions.first { it.name.startsWith("Getting started") })
          .withStartsAt(LocalDateTime.of(2026, 6, 15, 12, 0))
          .withEndsAt(LocalDateTime.of(2026, 6, 15, 14, 0))
          .withIsPlaceholder(false)
          .produce(),
      )

      val regularModuleSessions = moduleSessionTemplateRepository.findByModuleId(regularModuleId)

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(regularModuleSessions.first())
          .withStartsAt(LocalDateTime.of(2026, 7, 20, 15, 30))
          .withEndsAt(LocalDateTime.of(2026, 7, 20, 17, 30))
          .withIsPlaceholder(false)
          .produce(),
      )

      // When
      val schedule = service.getScheduleForGroup(group.id!!)

      // Then
      assertThat(schedule).isNotNull
      assertThat(schedule.preGroupOneToOneStartDate).isEqualTo("2026-06-01")
      assertThat(schedule.gettingStartedModuleStartDate).isEqualTo("2026-06-15")
      assertThat(schedule.endDate).isEqualTo("2026-07-20")

      assertThat(schedule.modules).isNotEmpty

      // Verify individual session formatting
      val gettingStartedSession = schedule.modules.find { it.name.startsWith("Getting started") }
      assertThat(gettingStartedSession).isNotNull
      assertThat(gettingStartedSession!!.time).isEqualTo("midday")
      assertThat(gettingStartedSession.type).isEqualTo("Individual")
      assertThat(gettingStartedSession.date).isEqualTo(schedule.gettingStartedModuleStartDate)

      val preGroupSession = schedule.modules.find { it.name.startsWith("Pre-group") }
      assertThat(preGroupSession!!.type).isEqualTo("Individual")
      assertThat(preGroupSession.time).isEqualTo("10am")
      assertThat(preGroupSession.date).isEqualTo(schedule.preGroupOneToOneStartDate)

      val lastSession = schedule.modules.last()
      assertThat(lastSession.date).isEqualTo(schedule.endDate)
    }

    @Test
    fun `should return group schedule with various times formatting if placeholder set`() {
      // Given
      val template = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      assertThat(template).isNotNull
      buildingChoicesTemplateId = template.id!!

      val modules = moduleRepository.findByAccreditedProgrammeTemplateId(buildingChoicesTemplateId)
      assertThat(modules).isNotEmpty

      // Find Pre-Group module
      val preGroupModule = modules.find { it.name.startsWith("Pre-group") }
      assertThat(preGroupModule).isNotNull
      preGroupModuleId = preGroupModule!!.id!!

      // Find Getting Started module
      val gettingStartedModule = modules.find { it.name.startsWith("Getting started") }
      assertThat(gettingStartedModule).isNotNull
      gettingStartedModuleId = gettingStartedModule!!.id!!

      // Find regular last module (module_number = last)
      val regularModule = modules.find { it.moduleNumber == modules.last().moduleNumber }
      assertThat(regularModule).isNotNull
      regularModuleId = regularModule!!.id!!

      val preGroupModuleSessions = moduleSessionTemplateRepository.findByModuleId(preGroupModuleId)

      // Create a test group associated with Building Choices template
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("SCHEDULE_GROUP")
          .withRegionName("Test Region")
          .withAccreditedProgrammeTemplate(template)
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )
      groupId = group.id!!

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(preGroupModuleSessions.first { it.name.startsWith("Pre-group") })
          .withStartsAt(LocalDateTime.of(2026, 6, 1, 10, 0))
          .withEndsAt(LocalDateTime.of(2026, 6, 1, 11, 0))
          .withIsPlaceholder(true)
          .produce(),
      )

      val gettingStartedModuleSessions = moduleSessionTemplateRepository.findByModuleId(gettingStartedModuleId)

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(gettingStartedModuleSessions.first { it.name.startsWith("Getting started") })
          .withStartsAt(LocalDateTime.of(2026, 6, 15, 12, 0))
          .withEndsAt(LocalDateTime.of(2026, 6, 15, 14, 0))
          .withIsPlaceholder(true)
          .produce(),
      )

      val regularModuleSessions = moduleSessionTemplateRepository.findByModuleId(regularModuleId)

      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(regularModuleSessions.first())
          .withStartsAt(LocalDateTime.of(2026, 7, 20, 15, 30))
          .withEndsAt(LocalDateTime.of(2026, 7, 20, 17, 30))
          .withIsPlaceholder(true)
          .produce(),
      )

      // When
      val schedule = service.getScheduleForGroup(group.id!!)

      // Then
      assertThat(schedule).isNotNull
      assertThat(schedule.preGroupOneToOneStartDate).isEqualTo("2026-06-01")
      assertThat(schedule.gettingStartedModuleStartDate).isEqualTo("2026-06-15")
      assertThat(schedule.endDate).isEqualTo("2026-07-20")

      assertThat(schedule.modules).isNotEmpty

      // Verify individual session formatting
      val gettingStartedSession = schedule.modules.find { it.name.startsWith("Getting started") }
      assertThat(gettingStartedSession).isNotNull
      assertThat(gettingStartedSession!!.time).isEqualTo("Various times")
      assertThat(gettingStartedSession.type).isEqualTo("Individual")
      assertThat(gettingStartedSession.date).isEqualTo(schedule.gettingStartedModuleStartDate)

      val preGroupSession = schedule.modules.find { it.name.startsWith("Pre-group") }
      assertThat(preGroupSession!!.type).isEqualTo("Individual")
      assertThat(preGroupSession.time).isEqualTo("Various times")
      assertThat(preGroupSession.date).isEqualTo(schedule.preGroupOneToOneStartDate)

      val lastSession = schedule.modules.last()
      assertThat(lastSession.date).isEqualTo(schedule.endDate)
    }

    @Test
    fun `should throw NotFoundException if group does not exist`() {
      val randomId = UUID.randomUUID()
      val exception = assertThrows<NotFoundException> {
        service.getScheduleForGroup(randomId)
      }
      assertThat(exception.message).isEqualTo("Group with id $randomId not found")
    }

    @Test
    fun `should throw NotFoundException when group has no sessions`() {
      // Given
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory().withCode("EMPTY_SCHED").produce(),
      )

      // When/Then
      val exception = assertThrows<NotFoundException> {
        service.getScheduleForGroup(group.id!!)
      }

      assertThat(exception.message).isEqualTo("No sessions found for group ${group.id}")
    }
  }
}
