package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.ATTC
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.UAAB
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private const val GETTING_STARTED_1 = "Introduction to Building Choices"
private const val GETTING_STARTED_2 = "Understanding myself"
private const val GETTING_STARTED_ONE_TO_ONE = "Getting started one-to-one"
private const val MANAGING_MYSELF_1 = "Understanding my feelings"

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
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var sessionAttendanceRepository: SessionAttendanceRepository

  @Autowired
  private lateinit var sessionAttendanceOutcomeTypeRepository: SessionAttendanceOutcomeTypeRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  private val may = ReportingFilter(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-31"))

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()

    // A Wednesday
    whenever(clock.instant()).thenReturn(Instant.parse("2026-06-10T11:00:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))
  }

  @Nested
  @DisplayName("Reports on groups that started between the dates")
  inner class GroupsStarted {

    private fun selectedGroupCodes(filter: ReportingFilter) = parseCsv(reportingService.getGroupSizeReportCsv(filter)).map { it.getValue("code") }

    @Test
    fun `should include groups with Getting started 1 on or between the start and end dates`() {
      createGroup("DAY_BEFORE", gettingStartedAt = "2026-04-30T23:30:00")
      createGroup("START_DATE", gettingStartedAt = "2026-05-01T00:00:00")
      createGroup("END_DATE", gettingStartedAt = "2026-05-31T18:30:00")
      createGroup("DAY_AFTER", gettingStartedAt = "2026-06-01T00:00:00")

      assertThat(selectedGroupCodes(may)).containsExactlyInAnyOrder("START_DATE", "END_DATE")
    }

    @Test
    fun `should include groups with Getting started 1 scheduled in the future`() {
      createGroup("FUTURE", gettingStartedAt = "2026-07-01T10:00:00")

      val july = ReportingFilter(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"))

      assertThat(selectedGroupCodes(july)).containsExactly("FUTURE")
    }

    @Test
    fun `should use the date of Getting started 1 rather than of its catch-ups`() {
      val group = createGroup("JUNE", gettingStartedAt = "2026-06-08T10:00:00")
      createSession(group, GETTING_STARTED_1, "2026-05-18T10:00:00", isCatchup = true)

      assertThat(selectedGroupCodes(may)).isEmpty()
    }

    @Test
    fun `should use the first date when Getting started 1 is scheduled more than once`() {
      val group = createGroup("APRIL", gettingStartedAt = "2026-04-20T10:00:00")
      createSession(group, GETTING_STARTED_1, "2026-05-04T10:00:00")

      assertThat(selectedGroupCodes(may)).isEmpty()
    }

    @Test
    fun `should not include groups that only have other sessions in the date range`() {
      val group = createGroup("NO_GETTING_STARTED_1", gettingStartedAt = null)
      createSession(group, GETTING_STARTED_2, "2026-05-11T10:00:00")

      assertThat(selectedGroupCodes(may)).isEmpty()
    }

    @Test
    fun `should not include deleted groups`() {
      val group = createGroup("DELETED", gettingStartedAt = "2026-05-11T10:00:00", deletedAt = "2026-05-12T10:00:00")
      val membership = createMembership(createReferral("X000001"), group)
      recordAttendance(createSession(group, GETTING_STARTED_2, "2026-05-12T10:00:00"), membership, ATTC)

      assertThat(selectedGroupCodes(may)).isEmpty()
      // The group size view leaves out deleted groups itself, so check a report that does not use it
      assertThat(parseCsv(reportingService.getDosageReportCsv(may))).isEmpty()
    }

    @Test
    fun `should only include groups in the region when one is given`() {
      createGroup("NORTH_EAST", gettingStartedAt = "2026-05-11T10:00:00", regionName = "North East Region")
      createGroup("WALES", gettingStartedAt = "2026-05-11T10:00:00", regionName = "Wales")

      assertThat(selectedGroupCodes(may.copy(regionName = "Wales"))).containsExactly("WALES")
    }

    @Test
    fun `should include groups in every region except test regions when no region is given`() {
      createGroup("NORTH_EAST", gettingStartedAt = "2026-05-11T10:00:00", regionName = "North East Region")
      createGroup("WALES", gettingStartedAt = "2026-05-11T10:00:00", regionName = "Wales")
      createGroup("BETA", gettingStartedAt = "2026-05-11T10:00:00", regionName = "PRIVATE BETA REGION")
      createGroup("DELETION_1", gettingStartedAt = "2026-05-11T10:00:00", regionName = "REGION FOR DELETION")
      createGroup("DELETION_2", gettingStartedAt = "2026-05-11T10:00:00", regionName = "REGION_FOR_DELETION")

      assertThat(selectedGroupCodes(may)).containsExactlyInAnyOrder("NORTH_EAST", "WALES")
    }

    @Test
    fun `should select the same groups for both reports`() {
      listOf("MATCH" to "2026-05-11T10:00:00", "TOO_EARLY" to "2026-04-13T10:00:00").forEach { (code, gettingStartedAt) ->
        val group = createGroup(code, gettingStartedAt)
        val referral = createReferral("CRN_$code")
        val membership = createMembership(referral, group, createdAt = "2026-04-01T09:00:00")
        val session = createSession(group, GETTING_STARTED_2, "2026-05-18T10:00:00", attendees = listOf(referral))
        recordAttendance(session, membership, ATTC)
      }

      listOf(reportingService.getGroupSizeReportCsv(may), reportingService.getDosageReportCsv(may)).forEach { csv ->
        assertThat(csv).contains("MATCH").doesNotContain("TOO_EARLY")
      }
    }

    @Test
    fun `should return only the header row when no groups match`() {
      listOf(reportingService.getGroupSizeReportCsv(may), reportingService.getDosageReportCsv(may)).forEach { csv ->
        assertThat(csv).isNotBlank().doesNotContain("\n")
      }
    }
  }

  @Nested
  @DisplayName("Reports on sessions held between the dates")
  inner class SessionsHeld {

    private fun reports(filter: ReportingFilter) = listOf(
      parseCsv(reportingService.getSessionRateReportCsv(filter)).map { it.getValue("groupCode") to it.getValue("weekStarting") },
      parseCsv(reportingService.getFacilitatorContinuityReportCsv(filter)).map {
        it.getValue("code") to LocalDate.parse(it.getValue("sessionStartTime").take(10)).with(DayOfWeek.MONDAY).toString()
      },
    )

    private fun createGroupWithSession(
      code: String,
      startsAt: String,
      regionName: String = "North East Region",
      deletedAt: String? = null,
      isPlaceholder: Boolean = false,
    ) {
      val group = createGroup(code, gettingStartedAt = null, regionName = regionName, deletedAt = deletedAt)
      val referral = createReferral("CRN_$code")
      createMembership(referral, group)
      createSession(group, GETTING_STARTED_2, startsAt, attendees = listOf(referral), isPlaceholder = isPlaceholder)
    }

    @Test
    fun `should include sessions on or between the start and end dates`() {
      createGroupWithSession("DAY_BEFORE", "2026-04-30T23:30:00")
      createGroupWithSession("START_DATE", "2026-05-01T00:00:00")
      createGroupWithSession("END_DATE", "2026-05-31T18:30:00")
      createGroupWithSession("DAY_AFTER", "2026-06-01T00:00:00")

      reports(may).forEach { sessions ->
        assertThat(sessions).containsExactlyInAnyOrder("START_DATE" to "2026-04-27", "END_DATE" to "2026-05-25")
      }
    }

    @Test
    fun `should include only the sessions between the dates, whenever the group started`() {
      val referral = createReferral("X000001")
      val group = createGroup("GROUP01", gettingStartedAt = "2026-03-09T10:00:00", gettingStartedAttendees = listOf(referral))
      createMembership(referral, group, createdAt = "2026-03-01T09:00:00")
      createSession(group, GETTING_STARTED_2, "2026-05-18T10:00:00", attendees = listOf(referral))
      createSession(group, MANAGING_MYSELF_1, "2026-06-02T10:00:00", attendees = listOf(referral))

      reports(may).forEach { sessions ->
        assertThat(sessions).containsExactly("GROUP01" to "2026-05-18")
      }
    }

    @Test
    fun `should include pre-group sessions between the dates for a group that has not started`() {
      val referral = createReferral("X000001")
      val group = createGroup("GROUP01", gettingStartedAt = "2026-06-15T10:00:00", gettingStartedAttendees = listOf(referral))
      createMembership(referral, group)
      createSession(group, "Pre-group one-to-one", "2026-05-18T10:00:00", attendees = listOf(referral))

      reports(may).forEach { sessions ->
        assertThat(sessions).containsExactly("GROUP01" to "2026-05-18")
      }
    }

    @Test
    fun `should not include sessions that have not been held, deleted groups or placeholder sessions`() {
      createGroupWithSession("HELD", "2026-06-10T11:30:00")
      createGroupWithSession("LATER_TODAY", "2026-06-10T12:30:00")
      createGroupWithSession("DELETED", "2026-06-09T10:00:00", deletedAt = "2026-06-10T09:00:00")
      createGroupWithSession("PLACEHOLDER", "2026-06-09T10:00:00", isPlaceholder = true)

      reports(ReportingFilter(LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-30"))).forEach { sessions ->
        assertThat(sessions).containsExactly("HELD" to "2026-06-08")
      }
    }

    @Test
    fun `should only include the region when one is given`() {
      createGroupWithSession("NORTH_EAST", "2026-05-11T10:00:00", regionName = "North East Region")
      createGroupWithSession("WALES", "2026-05-11T10:00:00", regionName = "Wales")

      reports(may.copy(regionName = "Wales")).forEach { sessions ->
        assertThat(sessions).containsExactly("WALES" to "2026-05-11")
      }
    }

    @Test
    fun `should include every region except test regions when no region is given`() {
      createGroupWithSession("NORTH_EAST", "2026-05-11T10:00:00", regionName = "North East Region")
      createGroupWithSession("WALES", "2026-05-11T10:00:00", regionName = "Wales")
      createGroupWithSession("BETA", "2026-05-11T10:00:00", regionName = "PRIVATE BETA REGION")

      reports(may).forEach { sessions ->
        assertThat(sessions).containsExactlyInAnyOrder("NORTH_EAST" to "2026-05-11", "WALES" to "2026-05-11")
      }
    }

    @Test
    fun `should return only the header row when no sessions match`() {
      listOf(reportingService.getSessionRateReportCsv(may), reportingService.getFacilitatorContinuityReportCsv(may)).forEach { csv ->
        assertThat(csv).isNotBlank().doesNotContain("\n")
      }
    }
  }

  @Nested
  @DisplayName("Group size")
  inner class GroupSize {

    @Test
    fun `should return group details followed by a column for each main session`() {
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T10:00:00")
      createMembership(createReferral("X000001"), group)

      val csv = reportingService.getGroupSizeReportCsv(may)

      val header = csv.lines().first().split(",")
      assertThat(header.take(14)).containsExactly(
        "id", "code", "createdAt", "sex", "cohort", "isLdc", "earliestPossibleStartDate", "regionName",
        "pduCode", "pduName", "locationCode", "locationName", "groupSize", "facilitatorStaffCode",
      )
      assertThat(csv.lines().first()).contains("M2 S1 Introduction to Building Choices", "M2 S2 Understanding myself")
      assertThat(csv.lines().first()).doesNotContain("one-to-one", "Post-programme review")

      val row = parseCsv(csv).single()
      assertThat(row).containsEntry("id", group.id.toString())
      assertThat(row).containsEntry("code", "GROUP01")
      assertThat(row).containsEntry("groupSize", "1")
      assertThat(row).containsEntry("facilitatorStaffCode", "FAC123")
    }

    @Test
    fun `should keep columns aligned when the location name contains a comma`() {
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T10:00:00", locationName = "Leeds Office, Park Row")
      createMembership(createReferral("X000001"), group)

      val row = parseCsv(reportingService.getGroupSizeReportCsv(may)).single()

      assertThat(row).containsEntry("locationName", "Leeds Office, Park Row")
      assertThat(row).containsEntry("groupSize", "1")
      assertThat(row).containsEntry("facilitatorStaffCode", "FAC123")
    }

    @Test
    fun `should count the PoPs scheduled for each main session that has been held`() {
      val referrals = listOf("X000001", "X000002", "X000003").map { createReferral(it) }
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T10:00:00", gettingStartedAttendees = referrals)
      createSession(group, GETTING_STARTED_2, "2026-05-18T10:00:00")
      createSession(group, MANAGING_MYSELF_1, "2026-06-10T12:30:00", attendees = referrals)

      val row = parseCsv(reportingService.getGroupSizeReportCsv(may)).single()

      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "3")
      assertThat(row).describedAs("held, but with nobody scheduled").containsEntry("M2 S2 Understanding myself", "0")
      assertThat(row).describedAs("later today").containsEntry("M3 S1 Understanding my feelings", "")
      assertThat(row).describedAs("not scheduled").containsEntry("M6 S1 Future me plan", "")
    }

    @Test
    fun `should count a PoP once when they are scheduled for a main session and its catch-up`() {
      val missedSession = createReferral("X000001")
      val attendedSession = createReferral("X000002")
      val joinedLate = createReferral("X000003")
      val group = createGroup(
        "GROUP01",
        gettingStartedAt = "2026-05-11T10:00:00",
        gettingStartedAttendees = listOf(missedSession, attendedSession),
      )
      createSession(group, GETTING_STARTED_1, "2026-05-13T10:00:00", isCatchup = true, attendees = listOf(missedSession, joinedLate))

      val row = parseCsv(reportingService.getGroupSizeReportCsv(may)).single()

      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "3")
    }
  }

  @Nested
  @DisplayName("Session rate")
  inner class SessionRate {

    @Test
    fun `should backfill gap weeks using the last known group location`() {
      val referral = createReferral("X000001", licReqNo = "LIC-111")
      val group = createGroup("GROUP01", gettingStartedAt = null)
      val membership = createMembership(referral, group)
      val firstSession = createSession(group, GETTING_STARTED_1, "2026-05-05T10:00:00", attendees = listOf(referral))
      recordAttendance(firstSession, membership, ATTC)
      val secondSession = createSession(group, GETTING_STARTED_2, "2026-05-19T10:00:00", attendees = listOf(referral))
      recordAttendance(secondSession, membership, UAAB)

      val lines = reportingService.getSessionRateReportCsv(may).lines()

      assertThat(lines).containsExactly(
        "licReqNo,crn,groupCode,regionName,pduName,deliveryLocationName,weekStarting,numberSessionAttended,numberSessionsNotAttended,numberSessionsScheduled",
        "LIC-111,X000001,GROUP01,\"North East Region\",\"Leeds PDU\",\"Leeds Office\",2026-05-04,1,0,1",
        "LIC-111,X000001,GROUP01,\"North East Region\",\"Leeds PDU\",\"Leeds Office\",2026-05-11,0,0,0",
        "LIC-111,X000001,GROUP01,\"North East Region\",\"Leeds PDU\",\"Leeds Office\",2026-05-18,0,1,1",
      )
    }

    @Test
    fun `should only count sessions that have been held`() {
      val referral = createReferral("X000001")
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-27T10:00:00", gettingStartedAttendees = listOf(referral))
      createMembership(referral, group)
      createSession(group, GETTING_STARTED_2, "2026-06-08T10:00:00", attendees = listOf(referral))
      createSession(group, MANAGING_MYSELF_1, "2026-06-10T11:30:00", attendees = listOf(referral))
      createSession(group, "Helpful and unhelpful feelings", "2026-06-10T12:30:00", attendees = listOf(referral))
      createSession(group, "Managing my feelings, part 1", "2026-06-12T10:00:00", attendees = listOf(referral))
      createSession(group, "Managing my feelings, part 2", "2026-07-06T10:00:00", attendees = listOf(referral))

      val rows = parseCsv(reportingService.getSessionRateReportCsv(may.copy(endDate = LocalDate.parse("2026-07-31"))))

      assertThat(rows.map { it.getValue("weekStarting") to it.getValue("numberSessionsScheduled") }).containsExactly(
        "2026-05-25" to "1",
        "2026-06-01" to "0",
        "2026-06-08" to "2",
      )
    }
  }

  @Nested
  @DisplayName("Facilitator continuity")
  inner class FacilitatorContinuity {

    @Test
    fun `should return sessions with their facilitators and attendee counts`() {
      val referrals = listOf("X000001", "X000002").map { createReferral(it) }
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T09:00:00", gettingStartedAttendees = referrals)
      createSession(group, GETTING_STARTED_ONE_TO_ONE, "2026-05-20T13:00:00", isCatchup = true, attendees = referrals.take(1), facilitatorCodes = listOf("FAC003"))
      createSession(group, GETTING_STARTED_2, "2026-06-25T10:00:00")

      val csv = reportingService.getFacilitatorContinuityReportCsv(may)

      val lines = csv.lines()
      assertThat(lines).hasSize(3)
      assertThat(lines[0]).isEqualTo(
        "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt",
      )
      assertThat(lines[1]).contains("GROUP01,2,\"Introduction to Building Choices\",group,false,2,FAC001,\"North East Region\",\"Leeds Office\",\"Leeds PDU\",\"2026-05-11 09:00\"")
      assertThat(lines[2]).contains("GROUP01,4,\"Getting started one-to-one\",one-to-one,true,1,FAC003,\"North East Region\",\"Leeds Office\",\"Leeds PDU\",\"2026-05-20 13:00\"")
    }

    @Test
    fun `should quote facilitator staff codes when there is more than one facilitator`() {
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T09:00:00")
      createSession(group, GETTING_STARTED_2, "2026-05-18T09:00:00", facilitatorCodes = listOf("FAC001", "FAC002"))

      assertThat(reportingService.getFacilitatorContinuityReportCsv(may)).contains("\"FAC001,FAC002\"")
    }
  }

  @Nested
  @DisplayName("Dosage")
  inner class Dosage {

    @Test
    fun `should return each group a PoP has been assigned to in the order they joined`() {
      val firstGroup = createGroup("GROUP-B", gettingStartedAt = "2026-05-11T10:00:00")
      val secondGroup = createGroup("GROUP-A", gettingStartedAt = "2026-05-12T10:00:00")
      val moved = createReferral("X000001", licReqNo = "LIC-123")
      createMembership(moved, firstGroup, createdAt = "2026-05-01T09:00:00", deletedAt = "2026-05-12T08:59:00")
      val movedMembership = createMembership(moved, secondGroup, createdAt = "2026-05-12T09:00:00")
      recordAttendance(createSession(secondGroup, GETTING_STARTED_2, "2026-05-19T10:00:00"), movedMembership, ATTC)
      val stayed = createReferral("X000002")
      val stayedMembership = createMembership(stayed, firstGroup, createdAt = "2026-05-01T09:00:00")
      recordAttendance(createSession(firstGroup, GETTING_STARTED_2, "2026-05-18T10:00:00"), stayedMembership, ATTC)

      val csv = reportingService.getDosageReportCsv(may)

      assertThat(csv.lines().first()).startsWith(
        "licReqNo,crn,groupCode1,groupCode2,regionName,numberSessionAttended,numberMainAttended,numberCatchUpAttended,M1 S1 Pre-group one-to-one,M2 S1 Introduction to Building Choices",
      )
      val rows = parseCsv(csv)
      assertThat(rows.map { it.getValue("crn") }).containsExactly("X000001", "X000002")
      assertThat(rows[0]).containsEntry("licReqNo", "LIC-123")
      assertThat(rows[0]).containsEntry("groupCode1", "GROUP-B")
      assertThat(rows[0]).containsEntry("groupCode2", "GROUP-A")
      assertThat(rows[0]).containsEntry("regionName", "North East Region")
      assertThat(rows[1]).containsEntry("groupCode1", "GROUP-B")
      assertThat(rows[1]).containsEntry("groupCode2", "")
    }

    @Test
    fun `should list a group once when a PoP leaves and rejoins it`() {
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T10:00:00")
      val referral = createReferral("X000001")
      createMembership(referral, group, createdAt = "2026-05-01T09:00:00", deletedAt = "2026-05-02T09:00:00")
      val membership = createMembership(referral, group, createdAt = "2026-05-03T09:00:00")
      recordAttendance(createSession(group, GETTING_STARTED_2, "2026-05-18T10:00:00"), membership, ATTC)

      val csv = reportingService.getDosageReportCsv(may)

      assertThat(csv.lines().first()).contains("groupCode1").doesNotContain("groupCode2")
      assertThat(parseCsv(csv).single()).containsEntry("groupCode1", "GROUP01")
    }

    @Test
    fun `should leave out PoPs who have not attended any sessions`() {
      val group = createGroup("GROUP01", gettingStartedAt = null)
      val session = createSession(group, GETTING_STARTED_1, "2026-05-11T10:00:00")
      recordAttendance(session, createMembership(createReferral("X000001"), group), ATTC)
      recordAttendance(session, createMembership(createReferral("X000002"), group), UAAB)
      val movedWithoutAttending = createReferral("X000003")
      createMembership(movedWithoutAttending, createGroup("GROUP02", gettingStartedAt = "2026-05-12T10:00:00"), deletedAt = "2026-05-12T17:00:00")
      createMembership(movedWithoutAttending, group, createdAt = "2026-05-13T09:00:00")

      val csv = reportingService.getDosageReportCsv(may)

      assertThat(parseCsv(csv).map { it.getValue("crn") }).containsExactly("X000001")
      assertThat(csv.lines().first()).describedAs("no column for a group only they were in").doesNotContain("groupCode2")
    }

    @Test
    fun `should total main sessions and catch-ups`() {
      val group = createGroup("GROUP01", gettingStartedAt = null)
      val membership = createMembership(createReferral("X000001"), group)
      listOf(
        // Main sessions
        createSession(group, GETTING_STARTED_1, "2026-05-11T10:00:00"),
        createSession(group, MANAGING_MYSELF_1, "2026-05-18T10:00:00"),
        createSession(group, "Managing my feelings, part 1", "2026-05-20T10:00:00"),
        // Main sessions that are also catch-ups
        createSession(group, GETTING_STARTED_2, "2026-05-13T10:00:00", isCatchup = true),
        createSession(group, "Helpful and unhelpful feelings", "2026-05-19T10:00:00", isCatchup = true),
        // One-to-ones
        createSession(group, GETTING_STARTED_ONE_TO_ONE, "2026-05-14T10:00:00"),
        createSession(group, "Managing myself one-to-one", "2026-05-21T10:00:00", isCatchup = true),
      ).forEach { recordAttendance(it, membership, ATTC) }
      recordAttendance(createSession(group, "Future me plan", "2026-05-27T10:00:00"), membership, UAAB)

      val row = parseCsv(reportingService.getDosageReportCsv(may)).single()

      assertThat(row).containsEntry("numberSessionAttended", "7")
      assertThat(row).containsEntry("numberMainAttended", "5")
      assertThat(row).containsEntry("numberCatchUpAttended", "3")
      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "GROUP01 - 2026-05-11 - Non-Catch Up")
      assertThat(row).containsEntry("M2 S2 Understanding myself", "GROUP01 - 2026-05-13 - Catch Up")
      assertThat(row).containsEntry("M2 S3 Getting started one-to-one", "GROUP01 - 2026-05-14 - Non-Catch Up")
      assertThat(row).containsEntry("M6 S1 Future me plan", "")
    }

    @Test
    fun `should include sessions held before the PoP was added to the group in the service`() {
      val group = createGroup("GROUP01", gettingStartedAt = null)
      val membership = createMembership(createReferral("X000001"), group, createdAt = "2026-05-20T09:00:00")
      recordAttendance(createSession(group, GETTING_STARTED_1, "2026-05-11T10:00:00"), membership, ATTC)

      val row = parseCsv(reportingService.getDosageReportCsv(may)).single()

      assertThat(row).containsEntry("numberSessionAttended", "1")
      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "GROUP01 - 2026-05-11 - Non-Catch Up")
    }

    @Test
    fun `should include sessions attended with groups outside the date range`() {
      val earlierGroup = createGroup("EARLIER", gettingStartedAt = null)
      val group = createGroup("GROUP01", gettingStartedAt = "2026-05-11T10:00:00")
      val referral = createReferral("X000001")
      val earlierMembership = createMembership(referral, earlierGroup, createdAt = "2026-03-01T09:00:00", deletedAt = "2026-04-01T09:00:00")
      createMembership(referral, group, createdAt = "2026-05-01T09:00:00")
      recordAttendance(createSession(earlierGroup, GETTING_STARTED_1, "2026-03-09T10:00:00"), earlierMembership, ATTC)

      val row = parseCsv(reportingService.getDosageReportCsv(may)).single()

      assertThat(row).containsEntry("groupCode1", "EARLIER")
      assertThat(row).containsEntry("groupCode2", "GROUP01")
      assertThat(row).containsEntry("numberSessionAttended", "1")
      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "EARLIER - 2026-03-09 - Non-Catch Up")
    }

    @Test
    fun `should use the latest attendance when it has been amended`() {
      val group = createGroup("GROUP01", gettingStartedAt = null)
      val membership = createMembership(createReferral("X000001"), group)
      val amendedToAbsent = createSession(group, GETTING_STARTED_1, "2026-05-11T10:00:00")
      recordAttendance(amendedToAbsent, membership, ATTC, createdAt = "2026-05-11T13:00:00")
      recordAttendance(amendedToAbsent, membership, UAAB, createdAt = "2026-05-12T09:00:00")
      val amendedToAttended = createSession(group, GETTING_STARTED_2, "2026-05-18T10:00:00")
      recordAttendance(amendedToAttended, membership, UAAB, createdAt = "2026-05-18T13:00:00")
      recordAttendance(amendedToAttended, membership, ATTC, createdAt = "2026-05-19T09:00:00")

      val row = parseCsv(reportingService.getDosageReportCsv(may)).single()

      assertThat(row).containsEntry("numberSessionAttended", "1")
      assertThat(row).containsEntry("M2 S1 Introduction to Building Choices", "")
      assertThat(row).containsEntry("M2 S2 Understanding myself", "GROUP01 - 2026-05-18 - Non-Catch Up")
    }
  }

  private fun parseCsv(csv: String): List<Map<String, String>> = CsvMapper()
    .readerForMapOf(String::class.java)
    .with(CsvSchema.emptySchema().withHeader())
    .readValues<Map<String, String>>(csv)
    .readAll()

  private fun createGroup(
    code: String,
    gettingStartedAt: String?,
    regionName: String = "North East Region",
    locationName: String = "Leeds Office",
    deletedAt: String? = null,
    gettingStartedAttendees: List<ReferralEntity> = emptyList(),
  ): ProgrammeGroupEntity {
    val factory = ProgrammeGroupFactory()
      .withCode(code)
      .withRegionName(regionName)
      .withProbationDeliveryUnit("Leeds PDU", "LDS01")
      .withDeliveryLocation(locationName, "LOC01")
      .withTreatmentManager(createFacilitator("FAC123"))
    deletedAt?.let { factory.withDeletedAt(LocalDateTime.parse(it)) }
    val group = programmeGroupRepository.save(factory.produce())

    gettingStartedAt?.let { createSession(group, GETTING_STARTED_1, it, attendees = gettingStartedAttendees) }
    return group
  }

  private fun createSession(
    group: ProgrammeGroupEntity,
    templateName: String,
    startsAt: String,
    isCatchup: Boolean = false,
    isPlaceholder: Boolean = false,
    attendees: List<ReferralEntity> = emptyList(),
    facilitatorCodes: List<String> = listOf("FAC001"),
  ): SessionEntity {
    val session = SessionFactory(group, moduleSessionTemplateRepository.findByName(templateName)!!)
      .withStartsAt(LocalDateTime.parse(startsAt))
      .withEndsAt(LocalDateTime.parse(startsAt).plusHours(2))
      .withIsCatchup(isCatchup)
      .withIsPlaceholder(isPlaceholder)
      .produce()
    session.sessionFacilitators = facilitatorCodes.map {
      SessionFacilitatorEntity(createFacilitator(it), session, FacilitatorType.REGULAR_FACILITATOR)
    }.toMutableSet()

    val savedSession = sessionRepository.save(session)
    attendees.forEach { testDataGenerator.createAttendee(it, savedSession) }
    return savedSession
  }

  private fun createFacilitator(code: String) = facilitatorRepository.save(FacilitatorEntityFactory().withNdeliusPersonCode(code).produce())

  private fun createReferral(crn: String, licReqNo: String = "LIC-001") = referralRepository.save(
    ReferralEntityFactory()
      .withInterventionName("Building Choices")
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId(licReqNo)
      .withCrn(crn)
      .produce(),
  )

  private fun createMembership(
    referral: ReferralEntity,
    group: ProgrammeGroupEntity,
    createdAt: String = "2026-05-01T09:00:00",
    deletedAt: String? = null,
  ): ProgrammeGroupMembershipEntity {
    val factory = ProgrammeGroupMembershipFactory(referral, group).withCreatedAt(LocalDateTime.parse(createdAt))
    deletedAt?.let { factory.withDeletedAt(LocalDateTime.parse(it)) }
    return programmeGroupMembershipRepository.save(factory.produce())
  }

  private fun recordAttendance(
    session: SessionEntity,
    membership: ProgrammeGroupMembershipEntity,
    outcomeCode: SessionAttendanceNDeliusCode,
    createdAt: String = "2026-05-31T09:00:00",
  ) {
    sessionAttendanceRepository.save(
      SessionAttendanceEntityFactory(session, membership)
        .withRecordedByFacilitator(createFacilitator("FAC001"))
        .withOutcomeType(sessionAttendanceOutcomeTypeRepository.findByCode(outcomeCode)!!)
        .withRecordedAt(LocalDateTime.parse(createdAt))
        .withCreatedAt(LocalDateTime.parse(createdAt))
        .produce(),
    )
  }
}
