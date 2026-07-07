package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
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
import java.time.format.DateTimeFormatter

class ReportingControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var facilitatorRepository: FacilitatorRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

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

  @Nested
  @DisplayName("Group Size endpoint")
  inner class GroupSizeEndpoint {

    @Test
    fun `should return group size reporting data in csv format with header and filename`() {
      // Given
      whenever(clock.instant()).thenReturn(Instant.parse("2026-05-18T12:30:00Z"))
      whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

      val group = ReportingGroupSizeTestDataHelper.createReportingGroup(
        referralRepository = referralRepository,
        facilitatorRepository = facilitatorRepository,
        programmeGroupRepository = programmeGroupRepository,
        programmeGroupMembershipRepository = programmeGroupMembershipRepository,
        groupCode = "GROUP01",
        facilitatorStaffCode = "FAC123",
        createdAt = LocalDateTime.parse("2026-05-10T10:00:00"),
        earliestStartDate = LocalDate.parse("2026-05-20"),
      )

      // When & Then
      val csvBody = webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/group-size.csv?groupStartedSince=2026-05-01T00:00:00")
        .headers(setAuthorisation())
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv"))
        .expectHeader().valueEquals(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"2026-05-18-13-30-manage-and-deliver-group-size.csv\"",
        )
        .expectBody<String>()
        .returnResult().responseBody!!

      val lines = csvBody.split("\n")
      assertThat(lines).hasSize(2)

      assertThat(lines.first()).isEqualTo(
        "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode",
      )
      assertThat(lines[1]).isEqualTo("${group.id},GROUP01,2026-05-10T10:00,MIXED,SEXUAL_OFFENCE,true,2026-05-20,North East,LDS01,Leeds PDU,LOC01,Leeds Office,1,FAC123")
    }

    @Disabled("Will be enabled when reporting role enforcement is added to this endpoint")
    @Test
    fun `should return unauthorized when user does not have reporting role`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/group-size.csv?groupStartedSince=2026-05-01T00:00:00")
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isUnauthorized
    }
  }

  @Nested
  @DisplayName("Facilitator Continuity endpoint")
  inner class FacilitatorContinuityEndpoint {

    @Test
    fun `should return facilitator continuity reporting data in csv format with header and filename`() {
      whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
      whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

      val template = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(template, "Module A", 1)
      val groupTemplate = testDataGenerator.createModuleSessionTemplate(
        module = module,
        name = "Session One",
        sessionNumber = 1,
        sessionType = SessionType.GROUP,
      )

      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withCode("THE_GROUP_CODE")
          .withRegionName("The Test Region")
          .withProbationDeliveryUnit("North East Test Region PDU", "NETRPDU")
          .withDeliveryLocation("Delivery Location, with a comma for Test 0123", "DLFT0123")
          .withCreatedAt(LocalDateTime.parse("2026-05-10T10:00:00"))
          .withAccreditedProgrammeTemplate(template)
          .produce(),
      )

      val facilitator = testDataGenerator.createFacilitator(
        FacilitatorEntityFactory()
          .withNdeliusPersonCode("FAC123")
          .produce(),
      )

      val session = SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(groupTemplate)
        .withStartsAt(LocalDateTime.parse("2026-05-20T09:00:00"))
        .withEndsAt(LocalDateTime.parse("2026-05-20T11:00:00"))
        .withCreatedAt(LocalDateTime.parse("2026-05-10T10:00:00"))
        .produce()

      session.sessionFacilitators = mutableSetOf(
        SessionFacilitatorEntity(
          facilitator = facilitator,
          session = session,
          facilitatorType = FacilitatorType.REGULAR_FACILITATOR,
        ),
      )

      val persistedSession = testDataGenerator.createSession(session)

      val referral = referralRepository.save(ReferralEntityFactory().produce())
      testDataGenerator.createAttendee(referral, session)

      val csvBody = webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/facilitator-continuity.csv?groupsCreatedSince=2026-05-01T00:00:00")
        .headers(setAuthorisation())
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv"))
        .expectHeader().valueEquals(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"2026-05-21-09-42-facilitator-continuity.csv\"",
        )
        .expectBody<String>()
        .returnResult().responseBody!!

      val lines = csvBody.split("\n")
      assertThat(lines).hasSize(2)
      assertThat(lines.first()).isEqualTo(
        "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt",
      )

      // We can't inline this b/c it's set by the DB integration
      var createdAtFormatted = session.createdAt!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

      assertThat(lines[1]).isEqualTo(
        "THE_GROUP_CODE,1,\"Session One\",group,false,1,FAC123,\"The Test Region\",\"Delivery Location, with a comma for Test 0123\",\"North East Test Region PDU\",\"2026-05-20 09:00\",\"$createdAtFormatted\"",
      )
    }

    @Test
    fun `should return bad request when facilitator continuity endpoint has no query params`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/facilitator-continuity.csv")
        .headers(setAuthorisation())
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Nested
  @DisplayName("Dosage endpoint")
  inner class DosageEndpoint {
    @Test
    fun `should return dosage reporting data in csv format with header and filename`() {
      whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
      whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

      val referral = referralRepository.save(
        ReferralEntityFactory()
          .withInterventionName("Building Choices")
          .withCreatedAt(LocalDateTime.parse("2026-05-10T09:00:00"))
          .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
          .withEventId("LIC-999")
          .withCrn("X12345")
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
          .withCreatedAt(LocalDateTime.parse("2026-05-20T09:00:00"))
          .produce(referral, referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()),
      )

      val group = programmeGroupRepository.save(
        ProgrammeGroupFactory()
          .withCode("GROUP01")
          .withRegionName("North East")
          .withEarliestStartDate(LocalDate.parse("2026-05-10"))
          .produce(),
      )
      val membership = programmeGroupMembershipRepository.save(
        ProgrammeGroupMembershipFactory(referral, group)
          .withCreatedAt(LocalDateTime.parse("2026-05-10T09:00:00"))
          .produce(),
      )
      val template = moduleSessionTemplateRepository.findByName("Introduction to Building Choices")!!
      val session = sessionRepository.save(
        SessionFactory(group, template)
          .withStartsAt(LocalDateTime.parse("2026-05-11T10:00:00"))
          .withEndsAt(LocalDateTime.parse("2026-05-11T12:00:00"))
          .produce(),
      )
      val attendedOutcome = sessionAttendanceOutcomeTypeRepository.findByCode(SessionAttendanceNDeliusCode.ATTC)!!
      val facilitator = facilitatorRepository.save(FacilitatorEntityFactory().produce())
      sessionAttendanceRepository.save(
        SessionAttendanceEntityFactory(session, membership)
          .withRecordedByFacilitator(facilitator)
          .withOutcomeType(attendedOutcome)
          .withRecordedAt(LocalDateTime.parse("2026-05-11T13:00:00"))
          .produce(),
      )

      val csvBody = webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/dosage.csv?referralsCreatedSince=2026-05-01")
        .headers(setAuthorisation())
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv"))
        .expectHeader().valueEquals(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"2026-05-21-09-42-dosage.csv\"",
        )
        .expectBody<String>()
        .returnResult().responseBody!!

      val lines = csvBody.split("\n")
      assertThat(lines.first()).contains("licReqNo,crn,numberSessionAttended")
      assertThat(lines.first()).contains("M2 S1 Introduction to Building Choices")
      assertThat(lines[1]).contains("LIC-999,X12345,1")
      assertThat(lines[1]).contains("GROUP01")
    }

    @Test
    fun `should return bad request when dosage endpoint missing both filters`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/reporting/dosage.csv")
        .headers(setAuthorisation())
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody<String>().value { body ->
          assertThat(body).contains("At least one of referralsCreatedSince or referralsCompletedAfter must be provided")
        }
    }
  }
}
