package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
import java.time.LocalDateTime

class GroupWaitlistViewRepositoryIntegrationTest(@Autowired private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository) : IntegrationTestBase() {

  @Autowired
  private lateinit var repo: GroupWaitlistItemViewRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    createReferralsWithStatusHistoryAndReportingLocations()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `retrieve group wait list items`() {
    val waitlistItems = repo.findAll()

    assertThat(waitlistItems.size).isEqualTo(4)
    assertThat(waitlistItems).allSatisfy { item ->
      assertThat(item)
        .hasFieldOrProperty("referralId")
        .hasFieldOrProperty("crn")
        .hasFieldOrProperty("personName")
        .hasFieldOrProperty("sentenceEndDate")
        .hasFieldOrProperty("sourcedFrom")
        .hasFieldOrProperty("cohort")
        .hasFieldOrProperty("hasLdc")
        .hasFieldOrProperty("dateOfBirth")
        .hasFieldOrProperty("sex")
        .hasFieldOrProperty("status")
        .hasFieldOrProperty("pduName")
        .hasFieldOrProperty("reportingTeam")
        .hasFieldOrProperty("activeProgrammeGroupId")
    }
  }

  @Test
  fun `retrieve latest active_programme_group_id`() {
    val waitlistItems = repo.findAll()
    val referralId = waitlistItems.first().referralId
    val group = programmeGroupService.createGroup("BCCD1")!!
    programmeGroupMembershipService.allocateReferralToGroup(referralId, group.id!!)

    val allocatedReferral = repo.findByIdOrNull(referralId)!!

    assertThat(allocatedReferral.activeProgrammeGroupId).isEqualTo(group.id)
  }

  private fun createReferralsWithStatusHistoryAndReportingLocations() {
    // Create referrals with associated status history and reporting locations
    val awaitingAssessmentStatusDescription =
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
    val programmeCompleteStatusDescription =
      referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()

    val referral1 = ReferralEntityFactory()
      .withPersonName("Joe Bloggs")
      .withCrn("X7182552")
      .withInterventionName("Horizon")
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()
    val referralReportingLocation1 = ReferralReportingLocationFactory(referral1)
      .withPduName("PDU1")
      .withReportingTeam("reportingTeam1")
      .withRegionName("WIREMOCKED REGION")
      .produce()
    val statusHistory1 = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.now())
      .withCreatedBy("USER_ID_12345")
      .withStartDate(LocalDateTime.now())
      .produce(referral1, awaitingAssessmentStatusDescription)

    val referral2 = ReferralEntityFactory()
      .withPersonName("Alex River")
      .withCrn("CRN-999999")
      .withInterventionName("Building Choices")
      .withCohort(OffenceCohort.SEXUAL_OFFENCE)
      .produce()
    val referralReportingLocation2 = ReferralReportingLocationFactory(referral2)
      .withPduName("PDU1")
      .withReportingTeam("reportingTeam2")
      .withRegionName("WIREMOCKED REGION")
      .produce()
    val statusHistory2 = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.now())
      .withCreatedBy("USER_ID_12345")
      .withStartDate(LocalDateTime.now())
      .produce(referral2, awaitingAssessmentStatusDescription)

    val referral3 = ReferralEntityFactory()
      .withPersonName("Jane Adams")
      .withCrn("CRN-888888")
      .withInterventionName("Building Choices")
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()
    val statusHistory3 = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.now())
      .withCreatedBy("USER_ID_12345")
      .withStartDate(LocalDateTime.now())
      .produce(referral3, awaitingAssessmentStatusDescription)
    val referralReportingLocation3 = ReferralReportingLocationFactory(referral3)
      .withPduName("PDU2")
      .withReportingTeam("reportingTeam1")
      .withRegionName("WIREMOCKED REGION")
      .produce()

    val referral4 = ReferralEntityFactory()
      .withPersonName("Pete Grims")
      .withCrn("CRN-777777")
      .withInterventionName("New Me")
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()
    val statusHistory4 = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.now())
      .withCreatedBy("USER_ID_12345")
      .withStartDate(LocalDateTime.now())
      .produce(referral4, programmeCompleteStatusDescription)
    val referralReportingLocation4 = ReferralReportingLocationFactory(referral4)
      .withPduName("PDU1")
      .withReportingTeam("reportingTeam1")
      .withRegionName("WIREMOCKED REGION")
      .produce()

    referral1.referralReportingLocationEntity = referralReportingLocation1
    referral2.referralReportingLocationEntity = referralReportingLocation2
    referral3.referralReportingLocationEntity = referralReportingLocation3
    referral4.referralReportingLocationEntity = referralReportingLocation4

    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral1,
      statusHistory1,
      referralReportingLocation1,
    )
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral2,
      statusHistory2,
      referralReportingLocation2,
    )
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral3,
      statusHistory3,
      referralReportingLocation3,
    )
    testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
      referral4,
      statusHistory4,
      referralReportingLocation4,
    )
  }
}
