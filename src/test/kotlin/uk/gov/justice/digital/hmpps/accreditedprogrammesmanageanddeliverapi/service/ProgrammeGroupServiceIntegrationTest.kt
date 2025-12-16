package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var service: ProgrammeGroupService

  @Autowired
  private lateinit var repository: ProgrammeGroupRepository

  @Nested
  @DisplayName("createGroup")
  inner class CreateGroup {
    @Test
    @Disabled("Not implemented yet")
    fun `should add a 3w gap for the Pre-group 1-1`() {
      // Create a group (at any time)
      // Expect there to be a 3w gap after the first session (i.e. the pre-group 1-1) and the
      // second session
      // --TJWC & JD 2025-12-16
      assert(false)
    }

    @Test
    fun `should the Sessions for a Programme Group`() {
      // Given
      stubAuthTokenEndpoint()
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
        CreateGroupSessionSlotFactory().withDayOfWeek(DayOfWeek.MONDAY).withHour(10).withMinute(0).withAmOrPm(AmOrPm.AM)
          .produce(),
        CreateGroupSessionSlotFactory().withDayOfWeek(DayOfWeek.FRIDAY).withHour(5).withMinute(30).withAmOrPm(AmOrPm.PM)
          .produce(),
      )

      val teamMembers = listOf(
        CreateGroupTeamMemberFactory().produce(teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER),
        CreateGroupTeamMemberFactory().produce(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR),
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
      val foundGroup = repository.findByCode("THE_GROUPCODE") ?: throw NotFoundException("Cannot find group")

      val friday28thAt17h30: Instant = LocalDateTime
        .of(2025, 3, 28, 17, 30)
        .atZone(ZoneId.of("UTC"))
        .toInstant()

      val friday4thAprilAt17h30InBst: Instant = LocalDateTime
        .of(2025, 4, 4, 16, 30)
        .atZone(ZoneId.of("UTC"))
        .toInstant()

      assertThat(
        foundGroup.sessions.find {
          it.startsAt.toInstant(ZoneOffset.UTC).equals(friday28thAt17h30)
        },
      ).isNotNull

      assertThat(
        foundGroup.sessions.find {
          it.startsAt.toInstant(ZoneOffset.UTC).equals(friday4thAprilAt17h30InBst)
        },
      ).isNotNull
    }
  }

  @Nested
  @DisplayName("getProgrammeGroupsForRegion")
  inner class GetProgrammeGroupsForRegion {
    val pageable: Pageable = Pageable.ofSize(10)

    @BeforeEach
    fun setup() {
      nDeliusApiStubs.clearAllStubs()

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
      )
      assertThat(programmeGroups.deliveryLocationNames).containsExactly("Location One")
    }
  }
}
