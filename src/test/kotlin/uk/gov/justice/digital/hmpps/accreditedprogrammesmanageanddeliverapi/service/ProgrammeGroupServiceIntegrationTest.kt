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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
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

    @AfterEach
    fun tearDown() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return the group schedule with correctly derived start and end dates`() {
      // Given
      val template = testDataGenerator.createAccreditedProgrammeTemplate("Building Choices")
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withCode("SCHEDULE_GROUP")
          .withRegionName("Region Description")
          .withAccreditedProgrammeTemplate(template)
          .produce(),
      )

      val module = testDataGenerator.createModule(template, "Module 1", 1)

      // 1. A Pre-group one-to-one session (Earliest)
      val preGroupTemplate = testDataGenerator.createModuleSessionTemplate(
        module,
        "Pre-group one-to-ones",
        1,
        sessionType = SessionType.ONE_TO_ONE,
        pathway = Pathway.MODERATE_INTENSITY,
        durationMinutes = 60,
      )
      testDataGenerator.createSession(
        SessionEntity(
          programmeGroup = group,
          moduleSessionTemplate = preGroupTemplate,
          startsAt = LocalDateTime.of(2026, 6, 1, 10, 0),
          endsAt = LocalDateTime.of(2026, 6, 1, 11, 0),
          isPlaceholder = false,
        ),
      )

      // 2. A Getting started session
      val gettingStartedTemplate = testDataGenerator.createModuleSessionTemplate(
        module,
        "Getting started session",
        2,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        durationMinutes = 120,
      )
      testDataGenerator.createSession(
        SessionEntity(
          programmeGroup = group,
          moduleSessionTemplate = gettingStartedTemplate,
          startsAt = LocalDateTime.of(2026, 6, 15, 12, 0),
          endsAt = LocalDateTime.of(2026, 6, 15, 14, 0),
          isPlaceholder = false,
        ),
      )

      // 3. A regular session (Latest)
      val regularTemplate = testDataGenerator.createModuleSessionTemplate(
        module,
        "Regular session",
        3,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        durationMinutes = 120,
      )
      testDataGenerator.createSession(
        SessionEntity(
          programmeGroup = group,
          moduleSessionTemplate = regularTemplate,
          startsAt = LocalDateTime.of(2026, 7, 20, 15, 30),
          endsAt = LocalDateTime.of(2026, 7, 20, 17, 30),
          isPlaceholder = false,
        ),
      )

      // When
      val schedule = service.getScheduleForGroup(group.id!!)

      // Then
      assertThat(schedule).isNotNull
      assertThat(schedule!!.preGroupOneToOneStartDate).isEqualTo("2026-06-01")
      assertThat(schedule.gettingStartedModuleStartDate).isEqualTo("2026-06-15")
      assertThat(schedule.endDate).isEqualTo("2026-07-20")

      assertThat(schedule.modules).hasSize(3)

      // Verify individual session formatting
      val middaySession = schedule.modules.find { it.name == "Getting started session" }
      assertThat(middaySession).isNotNull
      assertThat(middaySession!!.time).isEqualTo("midday")
      assertThat(middaySession.type).isEqualTo("Group")

      val individualSession = schedule.modules.find { it.name == "Pre-group one-to-ones" }
      assertThat(individualSession!!.type).isEqualTo("Individual")
      assertThat(individualSession.time).isEqualTo("10:0am")
    }

    @Test
    fun `should throw NotFoundException if group does not exist`() {
      val randomId = UUID.randomUUID()
      val exception = assertThrows<NotFoundException> {
        service.getScheduleForGroup(randomId)
      }
      assertThat(exception.message).isEqualTo("Group with id $randomId not found")
    }
  }
}
