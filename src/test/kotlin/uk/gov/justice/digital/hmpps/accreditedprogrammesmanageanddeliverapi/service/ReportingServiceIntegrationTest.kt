package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.MaterializedViewRefresher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReportingGroupSizeTestDataHelper
import java.time.LocalDate
import java.time.LocalDateTime

class ReportingServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var reportingService: ReportingService

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var facilitatorRepository: FacilitatorRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @Autowired
  private lateinit var materializedViewRefresher: MaterializedViewRefresher

  @Autowired
  private lateinit var referralReportingLocationRepository: ReferralReportingLocationRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var sessionAttendanceRepository: SessionAttendanceRepository

  @Autowired
  private lateinit var sessionAttendanceOutcomeTypeRepository: SessionAttendanceOutcomeTypeRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should return group size report csv for groups with earliest start after provided datetime`() {
    ReportingGroupSizeTestDataHelper.createReportingGroup(
      referralRepository = referralRepository,
      facilitatorRepository = facilitatorRepository,
      programmeGroupRepository = programmeGroupRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      groupCode = "GROUP_BEFORE",
      facilitatorStaffCode = "FAC001",
      createdAt = LocalDateTime.parse("2026-05-05T09:00:00"),
      earliestStartDate = LocalDate.parse("2026-05-01"),
    )

    ReportingGroupSizeTestDataHelper.createReportingGroup(
      referralRepository = referralRepository,
      facilitatorRepository = facilitatorRepository,
      programmeGroupRepository = programmeGroupRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      groupCode = "GROUP_AFTER",
      facilitatorStaffCode = "FAC002",
      createdAt = LocalDateTime.parse("2026-05-12T09:00:00"),
      earliestStartDate = LocalDate.parse("2026-05-20"),
    )

    materializedViewRefresher.refreshReportingGroupSizeView()

    val firstSessionAfter = LocalDateTime.parse("2026-05-10T00:00:00")

    val csv = reportingService.getGroupSizeReportCsv(firstSessionAfter)

    val lines = csv.split("\n")
    assertThat(lines).hasSize(2)
    assertThat(lines.first()).isEqualTo(
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode",
    )
    assertThat(lines[1]).contains("GROUP_AFTER")
    assertThat(lines[1]).contains("FAC002")
    assertThat(lines[1]).doesNotContain("GROUP_BEFORE")
  }

  @Test
  fun `should return dosage report csv with session columns and group codes per session`() {
    val referral = referralRepository.save(
      ReferralEntityFactory()
        .withInterventionName("Building Choices")
        .withCreatedAt(LocalDateTime.parse("2026-05-10T09:00:00"))
        .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
        .withEventId("LIC-123")
        .withCrn("CRN12345")
        .produce(),
    )
    referralReportingLocationRepository.save(
      ReferralReportingLocationFactory(referral)
        .withRegionName("North East")
        .withPduName("Leeds PDU")
        .withReportingTeam("Leeds Office")
        .produce(),
    )
    referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.parse("2026-05-22T09:00:00"))
        .produce(referral, referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()),
    )

    val firstGroup = programmeGroupRepository.save(
      ProgrammeGroupFactory()
        .withCode("GROUP-A")
        .withRegionName("North East")
        .withEarliestStartDate(LocalDate.parse("2026-05-10"))
        .produce(),
    )
    val secondGroup = programmeGroupRepository.save(
      ProgrammeGroupFactory()
        .withCode("GROUP-B")
        .withRegionName("North East")
        .withEarliestStartDate(LocalDate.parse("2026-05-12"))
        .produce(),
    )

    val firstMembership = programmeGroupMembershipRepository.save(
      ProgrammeGroupMembershipFactory(referral, firstGroup)
        .withCreatedAt(LocalDateTime.parse("2026-05-10T09:00:00"))
        .withDeletedAt(LocalDateTime.parse("2026-05-12T08:59:00"))
        .produce(),
    )
    val secondMembership = programmeGroupMembershipRepository.save(
      ProgrammeGroupMembershipFactory(referral, secondGroup)
        .withCreatedAt(LocalDateTime.parse("2026-05-12T09:00:00"))
        .produce(),
    )

    val introTemplate = moduleSessionTemplateRepository.findByName("Introduction to Building Choices")!!
    val understandingTemplate = moduleSessionTemplateRepository.findByName("Understanding myself")!!

    val attendedOutcome = sessionAttendanceOutcomeTypeRepository.findByCode(SessionAttendanceNDeliusCode.ATTC)!!
    val facilitator = facilitatorRepository.save(FacilitatorEntityFactory().produce())

    val introSession = sessionRepository.save(
      SessionFactory(firstGroup, introTemplate)
        .withStartsAt(LocalDateTime.parse("2026-05-11T10:00:00"))
        .withEndsAt(LocalDateTime.parse("2026-05-11T12:00:00"))
        .produce(),
    )
    val understandingSession = sessionRepository.save(
      SessionFactory(secondGroup, understandingTemplate)
        .withStartsAt(LocalDateTime.parse("2026-05-13T10:00:00"))
        .withEndsAt(LocalDateTime.parse("2026-05-13T12:00:00"))
        .produce(),
    )

    sessionAttendanceRepository.save(
      SessionAttendanceEntityFactory(introSession, firstMembership)
        .withRecordedByFacilitator(facilitator)
        .withOutcomeType(attendedOutcome)
        .withRecordedAt(LocalDateTime.parse("2026-05-11T13:00:00"))
        .produce(),
    )
    sessionAttendanceRepository.save(
      SessionAttendanceEntityFactory(understandingSession, secondMembership)
        .withRecordedByFacilitator(facilitator)
        .withOutcomeType(attendedOutcome)
        .withRecordedAt(LocalDateTime.parse("2026-05-13T13:00:00"))
        .produce(),
    )

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(
      referralsCreatedSince = LocalDate.parse("2026-05-01"),
      referralsCompletedAfter = null,
    )

    val lines = csv.split("\n")
    assertThat(lines.first()).contains("licReqNo,crn,numberSessionAttended")
    assertThat(lines.first()).contains("M2 S1 Introduction to Building Choices")
    assertThat(lines.first()).contains("M2 S2 Understanding myself")
    assertThat(lines[1]).contains("LIC-123,CRN12345,2")
    assertThat(lines[1]).contains("GROUP-A")
    assertThat(lines[1]).contains("GROUP-B")
  }
}
