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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDate

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var service: ProgrammeGroupService

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
          .withRegionName("Region Description")
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )

      testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("GROUP_TWO_CODE")
          .withProbationDeliveryUnit("Another PDU Description", "ANOTHER_PDU_CODE")
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
          deliveryLocation = null,
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
        deliveryLocation = null,
        cohort = null,
        sex = "MALE",
        selectedTab = GroupPageByRegionTab.NOT_STARTED,
        username = "the_username",
      )

      // Then
      assertThat(programmeGroups.pagedGroupData.totalElements).isEqualTo(2)
      assertThat(programmeGroups.pagedGroupData.map{it.code}).containsExactlyInAnyOrder("THE_GROUP_CODE", "GROUP_TWO_CODE")
    }

    @Test
    fun `Partial matching the group code`() {
      // When
      val programmeGroups = service.getProgrammeGroupsForRegion(
        pageable = Pageable.ofSize(10),
        groupCode = "THE_G",
        pdu = null,
        deliveryLocation = null,
        cohort = null,
        sex = null,
        selectedTab = GroupPageByRegionTab.NOT_STARTED,
        username = "the_username",
      )

      // Then
      assertThat(programmeGroups.pagedGroupData.totalElements).isEqualTo(1)
      assertThat(programmeGroups.pagedGroupData.first().code).isEqualTo("THE_GROUP_CODE")
    }
  }
}
