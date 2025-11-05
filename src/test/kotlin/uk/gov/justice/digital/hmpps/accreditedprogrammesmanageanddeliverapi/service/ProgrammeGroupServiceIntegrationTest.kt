package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDate
import java.util.UUID

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `createGroup can successfully create a new group`() {
    val group = ProgrammeGroupFactory().toCreateGroup()
    programmeGroupService.createGroup(group)
    val createdGroup = programmeGroupRepository.findByCode(group.groupCode)
    assertThat { createdGroup }.isNotNull
    assertThat(createdGroup?.code).isEqualTo(group.groupCode)
  }

  @Test
  fun `createGroup throws an error if a group already exists`() {
    val groupCode = "AAA111"
    val existingGroup = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(existingGroup)

    assertThrows<ConflictException> {
      programmeGroupService.createGroup(
        CreateGroup(
          existingGroup.code,
          ProgrammeGroupCohort.from(existingGroup.cohort, existingGroup.isLdc),
          existingGroup.sex,
        ),
      )
    }
  }

  @Test
  fun `getGroupWaitlistData returns empty page when no waitlist data exists`() {
    // Given
    val group = ProgrammeGroupFactory().withCode("TEST001").produce()
    testDataGenerator.createGroup(group)
    val pageable = PageRequest.of(0, 10)

    // When
    val result = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = null,
      nameOrCRN = null,
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    // Then
    assertThat(result).isNotNull
    assertThat(result.content).isEmpty()
  }

  @Test
  fun `getGroupWaitlistData throws NotFoundException for non-existent group`() {
    // Given
    val nonExistentGroupId = UUID.randomUUID()
    val pageable = PageRequest.of(0, 10)

    // When & Then
    assertThrows<NotFoundException> {
      programmeGroupService.getGroupWaitlistData(
        selectedTab = GroupPageTab.WAITLIST,
        groupId = nonExistentGroupId,
        sex = null,
        cohort = null,
        nameOrCRN = null,
        pdu = null,
        pageable = pageable,
        reportingTeam = null,
      )
    }
  }

  @Test
  fun `getGroupWaitlistData filters by sex correctly`() {
    // Given
    val group = setupGroupWaitListItemTestData()
    val pageable = PageRequest.of(0, 10)

    // When
    val maleResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = "Male",
      cohort = null,
      nameOrCRN = null,
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    val femaleResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = "Female",
      cohort = null,
      nameOrCRN = null,
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    // Then
    assertThat(maleResults.content).allMatch { it.sex == "Male" }
    assertThat(femaleResults.content).allMatch { it.sex == "Female" }
  }

  @Test
  fun `getGroupWaitlistData filters by cohort correctly`() {
    // Given
    val group = setupGroupWaitListItemTestData()
    val pageable = PageRequest.of(0, 10)

    // When
    val sexualOffenceResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = ProgrammeGroupCohort.SEXUAL,
      nameOrCRN = null,
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    val generalOffenceResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = ProgrammeGroupCohort.GENERAL,
      nameOrCRN = null,
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    // Then
    assertThat(sexualOffenceResults.content).allMatch { it.cohort == OffenceCohort.SEXUAL_OFFENCE }
    assertThat(generalOffenceResults.content).allMatch { it.cohort == OffenceCohort.GENERAL_OFFENCE }
  }

  @Test
  fun `getGroupWaitlistData filters by nameOrCRN correctly`() {
    // Given
    val group = setupGroupWaitListItemTestData()
    val pageable = PageRequest.of(0, 10)

    // When
    val nameResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = null,
      nameOrCRN = "john",
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    val crnResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = null,
      nameOrCRN = "CRN001",
      pdu = null,
      pageable = pageable,
      reportingTeam = null,
    )

    // Then
    assertThat(nameResults.content).allMatch {
      it.personName.contains("John", ignoreCase = true)
    }
    assertThat(crnResults.content).allMatch {
      it.crn.contains("CRN001", ignoreCase = true)
    }
  }

  @Test
  fun `getGroupWaitlistData filters by pdu correctly`() {
    // Given
    val group = setupGroupWaitListItemTestData()
    val pageable = PageRequest.of(0, 10)

    // When
    val pduResults = programmeGroupService.getGroupWaitlistData(
      selectedTab = GroupPageTab.WAITLIST,
      groupId = group.id!!,
      sex = null,
      cohort = null,
      nameOrCRN = null,
      pdu = "Test PDU 1",
      pageable = pageable,
      reportingTeam = null,
    )

    // Then
    assertThat(pduResults.content).allMatch { it.pdu == "Test PDU 1" }
  }

  private fun setupGroupWaitListItemTestData(): ProgrammeGroupEntity {
    val group = ProgrammeGroupFactory().withCode("TEST002").produce()
    testDataGenerator.createGroup(group)

    val referrals = listOf(
      createReferralWithWaitlistStatus("CRN001", "John Smith", "Male", "Sexual offence", "Test PDU 1", "Team A"),
      createReferralWithWaitlistStatus("CRN002", "Jane Doe", "Female", "General offence", "Test PDU 2", "Team B"),
      createReferralWithWaitlistStatus("CRN003", "John Brown", "Male", "Sexual offence", "Test PDU 1", "Team A"),
      createReferralWithWaitlistStatus("CRN004", "Mary Johnson", "Female", "General offence", "Test PDU 2", "Team B"),
      createReferralWithWaitlistStatus("CRN005", "Jonn Doe", "Male", "General offence", "Test PDU 1", "Team C"),
    )

    referrals.forEach { referral ->
      testDataGenerator.createReferralWithStatusHistory(referral.first, referral.second)
      if (referral.third != null) {
        testDataGenerator.createReferralWithReportingLocation(referral.third!!)
      }
      programmeGroupMembershipService.allocateReferralToGroup(referral.first.id!!, group.id!!)
    }

    return group
  }

  @Test
  fun `getGroupFilters returns empty filters when no referral reporting locations exist`() {
    // When
    val filters = programmeGroupService.getGroupFilters()

    // Then
    assertThat(filters.pduNames).isEmpty()
    assertThat(filters.reportingTeams).isEmpty()
    assertThat(filters.sex).containsExactly("Male", "Female")
  }

  @Test
  fun `getGroupFilters returns distinct pdu and reporting team values`() {
    // Given - Create referrals with reporting locations
    val referral1 = ReferralEntityFactory().withCrn("CRN001").produce()
    val referral2 = ReferralEntityFactory().withCrn("CRN002").produce()
    val referral3 = ReferralEntityFactory().withCrn("CRN003").produce()

    val statusHistory1 = ReferralStatusHistoryEntityFactory().produce(
      referral1,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )
    val statusHistory2 = ReferralStatusHistoryEntityFactory().produce(
      referral2,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )
    val statusHistory3 = ReferralStatusHistoryEntityFactory().produce(
      referral3,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )

    val reportingLocation1 = ReferralReportingLocationFactory()
      .withReferral(referral1)
      .withPduName("PDU Alpha")
      .withReportingTeam("Team 1")
      .produce()

    val reportingLocation2 = ReferralReportingLocationFactory()
      .withReferral(referral2)
      .withPduName("PDU Beta")
      .withReportingTeam("Team 2")
      .produce()

    val reportingLocation3 = ReferralReportingLocationFactory()
      .withReferral(referral3)
      .withPduName("PDU Alpha") // Duplicate PDU
      .withReportingTeam("Team 1") // Duplicate Team
      .produce()

    // Persist test data
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral1,
      statusHistory1,
      reportingLocation1,
    )
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral2,
      statusHistory2,
      reportingLocation2,
    )
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral3,
      statusHistory3,
      reportingLocation3,
    )

    // When
    val filters = programmeGroupService.getGroupFilters()

    // Then
    assertThat(filters.pduNames).containsExactlyInAnyOrder("PDU Alpha", "PDU Beta")
    assertThat(filters.reportingTeams).containsExactlyInAnyOrder("Team 1", "Team 2")
    assertThat(filters.sex).containsExactly("Male", "Female")
  }

  @Test
  fun `getGroupFilters handles multiple distinct values correctly`() {
    // Given
    val testData = listOf(
      Triple("CRN101", "North PDU", "North Team A"),
      Triple("CRN102", "South PDU", "South Team B"),
      Triple("CRN103", "East PDU", "East Team C"),
      Triple("CRN104", "West PDU", "West Team D"),
      Triple("CRN105", "North PDU", "North Team B"),
      Triple("CRN106", "South PDU", "South Team A"),
    )

    testData.forEach { (crn, pduName, reportingTeam) ->
      val referral = ReferralEntityFactory().withCrn(crn).produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referral,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      val reportingLocation = ReferralReportingLocationFactory()
        .withReferral(referral)
        .withPduName(pduName)
        .withReportingTeam(reportingTeam)
        .produce()

      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral,
        statusHistory,
        reportingLocation,
      )
    }

    // When
    val filters = programmeGroupService.getGroupFilters()

    // Then
    assertThat(filters.pduNames).containsExactlyInAnyOrder("North PDU", "South PDU", "East PDU", "West PDU")
    assertThat(filters.reportingTeams).containsExactlyInAnyOrder(
      "North Team A",
      "South Team B",
      "East Team C",
      "West Team D",
      "North Team B",
      "South Team A",
    )
    assertThat(filters.sex).containsExactly("Male", "Female")
  }

  private fun createReferralWithWaitlistStatus(
    crn: String,
    personName: String,
    sex: String,
    cohort: String,
    pduName: String,
    reportingTeam: String,
  ): Triple<ReferralEntity, ReferralStatusHistoryEntity, ReferralReportingLocationEntity?> {
    val referral = ReferralEntityFactory()
      .withCrn(crn)
      .withPersonName(personName)
      .withSex(sex)
      .withCohort(OffenceCohort.fromDisplayName(cohort))
      .withSentenceEndDate(LocalDate.now().plusYears(2))
      .withDateOfBirth(LocalDate.now().minusYears(30))
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .produce()

    val statusHistory = ReferralStatusHistoryEntityFactory().produce(
      referral,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )

    val reportingLocation = ReferralReportingLocationFactory()
      .withReferral(referral)
      .withPduName(pduName)
      .withReportingTeam(reportingTeam)
      .produce()

    return Triple(referral, statusHistory, reportingLocation)
  }
}
