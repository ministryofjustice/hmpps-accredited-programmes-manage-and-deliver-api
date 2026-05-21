package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.MaterializedViewRefresher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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
  fun `should return facilitator continuity report csv for happened sessions only`() {
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    val context = createGroupWithSessions(
      groupCode = "GROUP_A",
      groupCreatedAt = LocalDateTime.parse("2026-05-10T10:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-20T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-05-10T10:00:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-20T13:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-05-12T08:30:00"),
      includeFutureSession = true,
    )

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(
      groupsCreatedSince = LocalDateTime.parse("2026-05-01T00:00:00"),
      firstSessionAtOrAfter = null,
      lastSessionAtOrBefore = null,
    )

    val lines = csv.split("\n")
    assertThat(lines).hasSize(3)
    assertThat(lines.first()).isEqualTo(
      "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt",
    )
    assertThat(lines[1]).contains("GROUP_A,1")
    assertThat(lines[1]).contains("Session One")
    assertThat(lines[1]).contains(",group,false,2,${context.firstFacilitatorCode},TEST REGION,Delivery Location 1,Test PDU 1,2026-05-20 09:00,")
    assertThat(lines[2]).contains("GROUP_A,2")
    assertThat(lines[2]).contains("Session Two")
    assertThat(lines[2]).contains(",one-to-one,true,1,${context.secondFacilitatorCode},TEST REGION,Delivery Location 1,Test PDU 1,2026-05-20 13:00,")
    assertThat(csv).doesNotContain("Session Future")
  }

  @Test
  fun `should filter facilitator continuity report by groups created since`() {
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    createGroupWithSessions(
      groupCode = "GROUP_OLD",
      groupCreatedAt = LocalDateTime.parse("2026-04-25T09:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-10T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-04-25T09:30:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-10T12:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-04-26T09:00:00"),
      includeFutureSession = false,
    )

    createGroupWithSessions(
      groupCode = "GROUP_NEW",
      groupCreatedAt = LocalDateTime.parse("2026-05-15T09:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-20T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-05-15T09:30:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-20T12:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-05-16T09:00:00"),
      includeFutureSession = false,
    )

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(
      groupsCreatedSince = LocalDateTime.parse("2026-05-01T00:00:00"),
      firstSessionAtOrAfter = null,
      lastSessionAtOrBefore = null,
    )

    assertThat(csv).contains("GROUP_NEW")
    assertThat(csv).doesNotContain("GROUP_OLD")
  }

  @Test
  fun `should filter facilitator continuity report by first and last happened session boundaries`() {
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    createGroupWithSessions(
      groupCode = "GROUP_EARLY",
      groupCreatedAt = LocalDateTime.parse("2026-05-02T09:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-05T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-05-02T09:30:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-07T12:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-05-03T09:00:00"),
      includeFutureSession = false,
    )

    createGroupWithSessions(
      groupCode = "GROUP_MATCH",
      groupCreatedAt = LocalDateTime.parse("2026-05-10T09:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-15T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-05-10T09:30:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-20T12:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-05-11T09:00:00"),
      includeFutureSession = false,
    )

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(
      groupsCreatedSince = LocalDateTime.parse("2026-05-01T00:00:00"),
      firstSessionAtOrAfter = LocalDateTime.parse("2026-05-10T00:00:00"),
      lastSessionAtOrBefore = LocalDateTime.parse("2026-05-20T23:59:59"),
    )

    assertThat(csv).contains("GROUP_MATCH")
    assertThat(csv).doesNotContain("GROUP_EARLY")
  }

  @Test
  fun `should quote facilitator staff codes when multiple facilitators are present`() {
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    createGroupWithSessions(
      groupCode = "GROUP_MULTI",
      groupCreatedAt = LocalDateTime.parse("2026-05-10T10:00:00"),
      firstSessionAt = LocalDateTime.parse("2026-05-20T09:00:00"),
      firstSessionCreatedAt = LocalDateTime.parse("2026-05-10T10:00:00"),
      secondSessionAt = LocalDateTime.parse("2026-05-20T13:00:00"),
      secondSessionCreatedAt = LocalDateTime.parse("2026-05-12T08:30:00"),
      includeFutureSession = false,
      multipleFacilitatorsOnFirstSession = true,
    )

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(
      groupsCreatedSince = LocalDateTime.parse("2026-05-01T00:00:00"),
      firstSessionAtOrAfter = null,
      lastSessionAtOrBefore = null,
    )

    assertThat(csv).contains("FAC001,FAC002")
  }

  private fun createGroupWithSessions(
    groupCode: String,
    groupCreatedAt: LocalDateTime,
    firstSessionAt: LocalDateTime,
    firstSessionCreatedAt: LocalDateTime,
    secondSessionAt: LocalDateTime,
    secondSessionCreatedAt: LocalDateTime,
    includeFutureSession: Boolean,
    multipleFacilitatorsOnFirstSession: Boolean = false,
  ): TestGroupContext {
    val template = testDataGenerator.createAccreditedProgrammeTemplate("Programme $groupCode")
    val module = testDataGenerator.createModule(template, "Module $groupCode", 1)
    val groupTemplate = testDataGenerator.createModuleSessionTemplate(
      module = module,
      name = "Session One",
      sessionNumber = 1,
      sessionType = SessionType.GROUP,
    )
    val oneToOneTemplate = testDataGenerator.createModuleSessionTemplate(
      module = module,
      name = "Session Two",
      sessionNumber = 2,
      sessionType = SessionType.ONE_TO_ONE,
    )
    val futureTemplate = testDataGenerator.createModuleSessionTemplate(
      module = module,
      name = "Session Future",
      sessionNumber = 3,
      sessionType = SessionType.GROUP,
    )

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withCode(groupCode)
        .withCreatedAt(groupCreatedAt)
        .withAccreditedProgrammeTemplate(template)
        .produce(),
    )

    val facilitatorOne = testDataGenerator.createFacilitator(
      FacilitatorEntityFactory().withNdeliusPersonCode("FAC001").produce(),
    )
    val facilitatorTwo = testDataGenerator.createFacilitator(
      FacilitatorEntityFactory().withNdeliusPersonCode("FAC002").produce(),
    )
    val facilitatorThree = testDataGenerator.createFacilitator(
      FacilitatorEntityFactory().withNdeliusPersonCode("FAC003").produce(),
    )

    val firstSession = SessionFactory()
      .withProgrammeGroup(group)
      .withModuleSessionTemplate(groupTemplate)
      .withStartsAt(firstSessionAt)
      .withEndsAt(firstSessionAt.plusHours(2))
      .withCreatedAt(firstSessionCreatedAt)
      .produce()

    firstSession.sessionFacilitators = mutableSetOf(
      SessionFacilitatorEntity(
        facilitator = facilitatorOne,
        session = firstSession,
        facilitatorType = FacilitatorType.REGULAR_FACILITATOR,
      ),
    )

    if (multipleFacilitatorsOnFirstSession) {
      firstSession.sessionFacilitators.add(
        SessionFacilitatorEntity(
          facilitator = facilitatorTwo,
          session = firstSession,
          facilitatorType = FacilitatorType.COVER_FACILITATOR,
        ),
      )
    }

    testDataGenerator.createSession(firstSession)

    val secondSession = SessionFactory()
      .withProgrammeGroup(group)
      .withModuleSessionTemplate(oneToOneTemplate)
      .withStartsAt(secondSessionAt)
      .withEndsAt(secondSessionAt.plusHours(1))
      .withCreatedAt(secondSessionCreatedAt)
      .withIsCatchup(true)
      .produce()

    secondSession.sessionFacilitators = mutableSetOf(
      SessionFacilitatorEntity(
        facilitator = facilitatorThree,
        session = secondSession,
        facilitatorType = FacilitatorType.REGULAR_FACILITATOR,
      ),
    )

    testDataGenerator.createSession(secondSession)

    if (includeFutureSession) {
      val futureSession = SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(futureTemplate)
        .withStartsAt(LocalDateTime.parse("2026-06-25T10:00:00"))
        .withEndsAt(LocalDateTime.parse("2026-06-25T12:00:00"))
        .withCreatedAt(LocalDateTime.parse("2026-06-01T10:00:00"))
        .produce()

      futureSession.sessionFacilitators = mutableSetOf(
        SessionFacilitatorEntity(
          facilitator = facilitatorOne,
          session = futureSession,
          facilitatorType = FacilitatorType.REGULAR_FACILITATOR,
        ),
      )

      testDataGenerator.createSession(futureSession)
    }

    testDataGenerator.createAttendee(referralRepository.save(ReferralEntityFactory().produce()), firstSession)
    testDataGenerator.createAttendee(referralRepository.save(ReferralEntityFactory().produce()), firstSession)
    testDataGenerator.createAttendee(referralRepository.save(ReferralEntityFactory().produce()), secondSession)

    return TestGroupContext(
      firstFacilitatorCode = facilitatorOne.ndeliusPersonCode,
      secondFacilitatorCode = facilitatorThree.ndeliusPersonCode,
    )
  }

  private data class TestGroupContext(
    val firstFacilitatorCode: String,
    val secondFacilitatorCode: String,
  )

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
