package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.sar

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralMotivationBackgroundAndNonAssociationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.DeliveryLocationPreferenceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationProbationDeliveryUnitEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.BankHolidayRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DeliveryLocationPreferenceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationProbationDeliveryUnitRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralMotivationBackgroundAndNonAssociationsRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

class SarContractIntegrationTest :
  IntegrationTestBase(),
  SarApiDataTest,
  SarReportTest,
  SarFlywaySchemaTest,
  SarJpaEntitiesTest {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralMotivationBackgroundAndNonAssociationsRepository: ReferralMotivationBackgroundAndNonAssociationsRepository

  @Autowired
  private lateinit var deliveryLocationPreferenceRepository: DeliveryLocationPreferenceRepository

  @Autowired
  private lateinit var preferredDeliveryLocationRepository: PreferredDeliveryLocationRepository

  @Autowired
  private lateinit var preferredDeliveryLocationProbationDeliveryUnitRepository: PreferredDeliveryLocationProbationDeliveryUnitRepository

  @Autowired
  private lateinit var availabilityRepository: AvailabilityRepository

  @Autowired
  private lateinit var bankHolidayRepository: BankHolidayRepository

  @Autowired
  private lateinit var dataSource: DataSource

  @Autowired
  private lateinit var entityManager: EntityManager

  private val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
  private val dateTimeRegex = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?")
  private val humanDateTimeRegex = Regex("\\d{1,2} \\w+ \\d{4}, \\d{1,2}:\\d{2}:\\d{2} (?:am|pm)")

  private val generateActual = System.getenv("SAR_GENERATE_ACTUAL").toBoolean()

  private val fixedNow = LocalDateTime.of(2026, 5, 13, 12, 0)
  private val fixedDob = LocalDate.of(1994, 4, 13)
  private val fixedSentenceEndDate = LocalDate.of(2028, 5, 12)

  private val sarIntegrationTestHelper by lazy {
    SarIntegrationTestHelper(
      jwtAuthHelper = jwtAuthHelper,
      expectedApiResponsePath = "/sar/sar-api-response.json",
      expectedRenderResultPath = "/sar/sar-expected-render-result.html",
      attachmentsExpected = false,
      expectedFlywaySchemaVersion = "106",
      expectedJpaEntitySchemaPath = "/sar/entity-schema-snapshot.json",
    )
  }

  override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper

  override fun getWebTestClientInstance(): WebTestClient = webTestClient

  override fun getDataSourceInstance(): DataSource = dataSource

  override fun getEntityManagerInstance(): EntityManager = entityManager

  override fun getCrn(): String = "X123456"

  override fun setupTestData() {
    testDataCleaner.cleanAllTables()
    govUkApiStubs.stubBankHolidaysResponse()
    nDeliusApiStubs.stubUserTeamsResponse(
      "AUTH_ADM",
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
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

    val referrals = testReferralHelper.createReferrals(
      referralConfigs = listOf(
        TestReferralHelper.ReferralConfig(
          reportingPdu = "PDU 1",
          reportingTeam = "Team A",
          regionName = "WIREMOCKED REGION",
          crn = getCrn(),
          personName = "Test Person SAR",
          dateOfBirth = fixedDob,
          sentenceEndDate = fixedSentenceEndDate,
        ),
      ),
    )

    val referral = referrals[0]
    referral.referralCohortHistories.add(
      ReferralCohortHistoryFactory()
        .withReferral(referral)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .withCreatedBy("AUTH_USER")
        .produce(),
    )
    referralRepository.saveAndFlush(referral)

    val motivationBackgroundAndNonAssociation = ReferralMotivationBackgroundAndNonAssociationsFactory()
      .withReferral(referral)
      .withMaintainsInnocence(false)
      .withMotivations("Motivation details for SAR")
      .withNonAssociations("Non association details for SAR")
      .withOtherConsents("Other considerations for SAR")
      .withCreatedBy("AUTH_USER")
      .withLastUpdatedBy("AUTH_USER")
      .withLastUpdatedAt(fixedNow)
      .produce()
    referralMotivationBackgroundAndNonAssociationsRepository.saveAndFlush(motivationBackgroundAndNonAssociation)

    val preferredPdu = preferredDeliveryLocationProbationDeliveryUnitRepository.saveAndFlush(
      PreferredDeliveryLocationProbationDeliveryUnitEntityFactory()
        .withDeliusCode("PDU001")
        .withDeliusDescription("Test PDU 1")
        .produce(),
    )
    val preferredDeliveryLocation = preferredDeliveryLocationRepository.saveAndFlush(
      PreferredDeliveryLocationEntityFactory()
        .withPreferredDeliveryLocationProbationDeliveryUnit(preferredPdu)
        .withDeliusCode("DLC001")
        .withDeliusDescription("Test Delivery Location")
        .produce(),
    )
    val deliveryLocationPreference = DeliveryLocationPreferenceEntityFactory(referral, preferredPdu)
      .withCreatedBy("AUTH_USER")
      .withCreatedAt(fixedNow)
      .withLastUpdatedAt(fixedNow)
      .withLocationsCannotAttendText("Cannot attend at weekends")
      .withPreferredDeliveryLocations(mutableSetOf(preferredDeliveryLocation))
      .produce()
    deliveryLocationPreferenceRepository.saveAndFlush(deliveryLocationPreference)

    val availability = AvailabilityEntity(
      referral = referral,
      startDate = LocalDate.of(2026, 6, 1),
      endDate = LocalDate.of(2026, 8, 31),
      otherDetails = "Available weekdays only",
      lastModifiedBy = "AUTH_USER",
      lastModifiedAt = fixedNow,
    )
    availability.slots.addAll(
      listOf(
        AvailabilitySlotEntity(dayOfWeek = DayOfWeek.MONDAY, slotName = SlotName.DAYTIME, availability = availability),
      ),
    )
    availabilityRepository.saveAndFlush(availability)

    val status = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    referralService.updateStatus(referral, status.id, createdBy = "AUTH_USER")

    val messageHistory = MessageHistoryEntity(
      null,
      "event 1",
      "www.test.com/event_1",
      "test description",
      fixedNow,
      "test message",
      fixedNow,
      referral,
    )
    messageHistoryRepository.saveAndFlush(messageHistory)

    val slot = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val createGroupRequest = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.of(2026, 6, 8),
      createGroupSessionSlot = setOf(slot),
      pduName = "PDU 1",
      pduCode = "PDU001",
      deliveryLocationName = "Test Delivery Location",
      deliveryLocationCode = "DLC001",
      teamMembers = listOf(
        CreateGroupTeamMemberFactory().produceWithRandomValues(
          personName = "Deterministic Facilitator",
          personCode = "FAC001",
          ndeliusTeamName = "Deterministic Team",
          ndeliusTeamCode = "TEAM001",
          teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER,
        ),
      ),
    )

    performRequestAndExpectStatus(
      httpMethod = org.springframework.http.HttpMethod.POST,
      uri = "/group",
      body = createGroupRequest,
      expectedResponseStatus = org.springframework.http.HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(createGroupRequest.groupCode)!!

    performRequestAndExpectStatus(
      httpMethod = org.springframework.http.HttpMethod.POST,
      uri = "/group/${group.id}/allocate/${referral.id}",
      body = AllocateToGroupRequest(additionalDetails = "Test allocation"),
      expectedResponseStatus = org.springframework.http.HttpStatus.OK.value(),
    )

    val groupWithAllocation = programmeGroupRepository.findByCode(createGroupRequest.groupCode)!!
    val session = groupWithAllocation.sessions.first { !it.isPlaceholder }

    stubAuthTokenEndpoint()
    val groupMembership = groupWithAllocation.programmeGroupMemberships.first { it.referral.id == referral.id }

    val attendance = SessionAttendanceEntity(
      session = session,
      groupMembership = groupMembership,
      outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().produce(),
    ).apply {
      notesHistory.add(SessionNotesHistoryEntity(attendance = this, notes = "Notes for referral"))
    }

    session.attendances.add(attendance)
    sessionRepository.saveAndFlush(session)
  }

  @Test
  override fun `SAR API should return expected data`() {
    setupTestData()

    val response = sarIntegrationTestHelper.requestSarDataForCrn(getCrn(), webTestClient)
    val normalizedActual = normalizeDynamicValues(sarIntegrationTestHelper.toJson(response))

    if (generateActual) {
      sarIntegrationTestHelper.saveContentToFile(normalizedActual, "sar-api-response.json.log")
    } else {
      assertThat(normalizedActual).isEqualTo(normalizeDynamicValues(sarIntegrationTestHelper.getExpectedSarJson()))
    }
  }

  @Test
  override fun `SAR report should render as expected`() {
    setupTestData()

    // These are needed for the Sar helper functions
    sarIntegrationTestHelper.stubFindPrisonNameWith("Moorland (HMP & YOI)")
    sarIntegrationTestHelper.stubFindUserLastNameWith("Johnson")
    sarIntegrationTestHelper.stubFindLocationNameByNomisIdWith("PROPERTY BOX 1")
    sarIntegrationTestHelper.stubFindLocationNameByDpsIdWith("PROPERTY BOX 2")

    val dataResponse = sarIntegrationTestHelper.requestSarDataForCrn(getCrn(), webTestClient)
    val templateResponse = sarIntegrationTestHelper.requestSarTemplate(webTestClient)

    val renderResult = sarIntegrationTestHelper.renderServiceReport(
      data = dataResponse.content,
      templateVersion = "1.0",
      template = templateResponse,
    )

    sarIntegrationTestHelper.renderAndSaveReportAsPdf(renderResult, prn = null, crn = getCrn())

    val normalizedActual = normalizeDynamicValues(renderResult)

    if (generateActual) {
      sarIntegrationTestHelper.saveContentToFile(normalizedActual, "sar-generated-report.html.log")
    } else {
      sarIntegrationTestHelper.assertHtmlEquals(
        normalizedActual,
        normalizeDynamicValues(sarIntegrationTestHelper.getExpectedRenderResult()),
      )
    }
  }

  private fun normalizeDynamicValues(value: String): String = value
    .replace(uuidRegex, "<UUID>")
    .replace(dateTimeRegex, "<DATE_TIME>")
    .replace(humanDateTimeRegex, "<DATE_TIME>")

  @BeforeEach
  override fun beforeEach() {
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-13T12:00:00Z"))
    whenever(clock.zone).thenReturn(ZoneOffset.UTC)

    testDataCleaner.cleanAllTables()
    nDeliusApiStubs.clearAllStubs()
    govUkApiStubs.stubBankHolidaysResponse()
  }
}
