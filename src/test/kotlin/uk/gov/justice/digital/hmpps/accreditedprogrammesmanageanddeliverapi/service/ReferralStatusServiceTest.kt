package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.Constants.MODULE_NAME_PREFIX_PRE_GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusTransitionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.ON_PROGRAMME
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.RETURN_TO_COURT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.time.LocalDateTime
import java.util.UUID

class ReferralStatusServiceTest {

  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository = mockk()
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository = mockk()
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository = mockk()
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository = mockk()
  private val referralRepository: ReferralRepository = mockk()
  private val sessionAttendanceRepository: SessionAttendanceRepository = mockk()
  private val applicationEventPublisher: ApplicationEventPublisher = mockk()
  private val userService: UserService = mockk()

  private lateinit var referralStatusService: ReferralStatusService

  @BeforeEach
  fun beforeEach() {
    referralStatusService = ReferralStatusService(
      referralStatusTransitionRepository = referralStatusTransitionRepository,
      referralStatusHistoryRepository = referralStatusHistoryRepository,
      referralStatusDescriptionRepository = referralStatusDescriptionRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      referralRepository = referralRepository,
      sessionAttendanceRepository = sessionAttendanceRepository,
      applicationEventPublisher = applicationEventPublisher,
      userService = userService,
    )
  }

  @Nested
  @DisplayName("getStatusTransitionsForReferral")
  inner class GetStatusTransitionsForReferral {

    @Test
    fun `should return null when no status history exists for referral`() {
      // Given
      val referralId = UUID.randomUUID()
      every {
        referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId)
      } returns null

      // When
      val result = referralStatusService.getStatusTransitionsForReferral(referralId)

      // Then
      assertThat(result).isNull()
      verify { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) }
    }

    @Test
    fun `should return status transitions without current group details when referral is not in a group`() {
      // Given
      val referralId = UUID.randomUUID()
      val referral = ReferralEntityFactory().withId(referralId).withPersonName("Alex River").produce()

      val currentStatusDescription = ReferralStatusDescriptionEntityFactory()
        .withReferralStatusType(AWAITING_ASSESSMENT)
        .withLabelColour("blue")
        .produce()

      val currentStatusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedBy("user_1")
        .withCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
        .produce(referral, currentStatusDescription)

      val toStatus1 = ReferralStatusDescriptionEntityFactory().withReferralStatusType(ON_PROGRAMME).produce()
      val toStatus2 = ReferralStatusDescriptionEntityFactory().withReferralStatusType(RETURN_TO_COURT).produce()

      val transition1 = ReferralStatusTransitionEntityFactory()
        .withFromStatus(currentStatusDescription)
        .withToStatus(toStatus1)
        .withDescription("Start assessment")
        .withPriority(1)
        .produce()

      val transition2 = ReferralStatusTransitionEntityFactory()
        .withFromStatus(currentStatusDescription)
        .withToStatus(toStatus2)
        .withDescription("Mark as not eligible")
        .withPriority(2)
        .produce()

      val suggestedTransition = ReferralStatusTransitionEntityFactory()
        .withFromStatus(currentStatusDescription)
        .withToStatus(toStatus1)
        .withIsSuggested(true)
        .produce()

      every {
        referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId)
      } returns currentStatusHistory

      every {
        referralStatusTransitionRepository.findByFromStatusIdAndIsVisibleTrueOrderByPriorityAsc(currentStatusDescription.id)
      } returns mutableListOf(transition1, transition2)

      every {
        referralStatusTransitionRepository.findFirstByFromStatusIdAndIsSuggestedTrue(currentStatusDescription.id)
      } returns suggestedTransition

      every {
        programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      } returns null

      // When
      val result = referralStatusService.getStatusTransitionsForReferral(referralId)

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.currentStatus.statusDescriptionId).isEqualTo(currentStatusDescription.id)
      assertThat(result.currentStatus.title).isEqualTo("Awaiting assessment")
      assertThat(result.currentStatus.tagColour).isEqualTo("blue")
      assertThat(result.currentStatus.updatedByName).isEqualTo("user_1")
      assertThat(result.currentStatus.createdAt).isEqualTo(LocalDateTime.of(2026, 4, 1, 10, 0))

      assertThat(result.availableStatuses).hasSize(2)
      assertThat(result.availableStatuses[0].id).isEqualTo(toStatus1.id)
      assertThat(result.availableStatuses[0].status).isEqualTo("On programme")
      assertThat(result.availableStatuses[0].transitionDescription).isEqualTo("Start assessment")
      assertThat(result.availableStatuses[1].id).isEqualTo(toStatus2.id)
      assertThat(result.availableStatuses[1].status).isEqualTo("Return to court")
      assertThat(result.availableStatuses[1].transitionDescription).isEqualTo("Mark as not eligible")

      assertThat(result.suggestedStatus).isNotNull
      val suggested = result.suggestedStatus!!
      assertThat(suggested.statusDescriptionId).isEqualTo(toStatus1.id)
      assertThat(suggested.name).isEqualTo(toStatus1.referralStatusType.displayName)

      assertThat(result.currentGroupDetails).isNull()
    }

    @Test
    fun `should return suggestedStatus as null when no suggested transition exists`() {
      // Given
      val referralId = UUID.randomUUID()
      val referral = ReferralEntityFactory().withId(referralId).produce()
      val currentStatusDescription = ReferralStatusDescriptionEntityFactory().produce()
      val currentStatusHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatusDescription)

      every {
        referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId)
      } returns currentStatusHistory

      every {
        referralStatusTransitionRepository.findByFromStatusIdAndIsVisibleTrueOrderByPriorityAsc(currentStatusDescription.id)
      } returns mutableListOf()

      every {
        referralStatusTransitionRepository.findFirstByFromStatusIdAndIsSuggestedTrue(currentStatusDescription.id)
      } returns null

      every {
        programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      } returns null

      // When
      val result = referralStatusService.getStatusTransitionsForReferral(referralId)

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.suggestedStatus).isNull()
      assertThat(result.availableStatuses).isEmpty()
    }

    @Test
    fun `should return currentGroupDetails with only one-to-one pre-group attendances when referral is in a group`() {
      // Given
      val referralId = UUID.randomUUID()
      val groupId = UUID.randomUUID()
      val groupCode = "GRP-100"

      val referral = ReferralEntityFactory()
        .withId(referralId)
        .withPersonName("Alex River")
        .produce()

      val currentStatusDescription = ReferralStatusDescriptionEntityFactory().produce()
      val currentStatusHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatusDescription)

      val programmeGroup = ProgrammeGroupFactory()
        .withId(groupId)
        .withCode(groupCode)
        .produce()

      val membership = ProgrammeGroupMembershipFactory()
        .withId(UUID.randomUUID())
        .withProgrammeGroup(programmeGroup)
        .withReferral(referral)
        .produce()

      // 1. One-to-one Pre-group session (should be included)
      val preGroupModule = ModuleEntityFactory().withName("$MODULE_NAME_PREFIX_PRE_GROUP 1").produce()
      val preGroupTemplate = ModuleSessionTemplateEntityFactory()
        .withModule(preGroupModule)
        .withSessionType(SessionType.ONE_TO_ONE)
        .withName("Pre-group 1: Introduction")
        .produce()
      val preGroupSession = SessionFactory(programmeGroup = programmeGroup, moduleSessionTemplate = preGroupTemplate)
        .withId(UUID.randomUUID())
        .withStartsAt(LocalDateTime.of(2026, 6, 1, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 6, 1, 11, 0))
        .produce()
      val preGroupOutcome = SessionAttendanceNDeliusOutcomeEntityFactory()
        .withCode(SessionAttendanceNDeliusCode.ATTC)
        .withDescription("Attended - Complied")
        .produce()
      val preGroupAttendance = SessionAttendanceEntityFactory(session = preGroupSession, groupMembership = membership)
        .withOutcomeType(preGroupOutcome)
        .produce()

      // 2. Group Pre-group session (should be excluded because sessionType is GROUP)
      val groupPreGroupTemplate = ModuleSessionTemplateEntityFactory()
        .withModule(preGroupModule)
        .withSessionType(SessionType.GROUP)
        .withName("Pre-group Group Session")
        .produce()
      val groupPreGroupSession =
        SessionFactory(programmeGroup = programmeGroup, moduleSessionTemplate = groupPreGroupTemplate)
          .withId(UUID.randomUUID())
          .produce()
      val groupPreGroupAttendance =
        SessionAttendanceEntityFactory(session = groupPreGroupSession, groupMembership = membership)
          .produce()

      // 3. One-to-one non-Pre-group session (should be excluded because moduleName does not start with Pre-group)
      val mainModule = ModuleEntityFactory().withName("Main Module 1").produce()
      val mainTemplate = ModuleSessionTemplateEntityFactory()
        .withModule(mainModule)
        .withSessionType(SessionType.ONE_TO_ONE)
        .withName("Main 1: Getting Started")
        .produce()
      val mainSession = SessionFactory(programmeGroup = programmeGroup, moduleSessionTemplate = mainTemplate)
        .withId(UUID.randomUUID())
        .produce()
      val mainAttendance = SessionAttendanceEntityFactory(session = mainSession, groupMembership = membership)
        .produce()

      membership.attendances = mutableSetOf(preGroupAttendance, groupPreGroupAttendance, mainAttendance)

      every {
        referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId)
      } returns currentStatusHistory

      every {
        referralStatusTransitionRepository.findByFromStatusIdAndIsVisibleTrueOrderByPriorityAsc(currentStatusDescription.id)
      } returns mutableListOf()

      every {
        referralStatusTransitionRepository.findFirstByFromStatusIdAndIsSuggestedTrue(currentStatusDescription.id)
      } returns null

      every {
        programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      } returns membership

      // When
      val result = referralStatusService.getStatusTransitionsForReferral(referralId)

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.currentGroupDetails).isNotNull
      val groupDetails = result.currentGroupDetails!!
      assertThat(groupDetails.currentlyAllocatedGroupId).isEqualTo(groupId)
      assertThat(groupDetails.currentlyAllocatedGroupCode).isEqualTo(groupCode)
      assertThat(groupDetails.attendanceHistories).hasSize(1)

      val sessionHistory = groupDetails.attendanceHistories.first()
      assertThat(sessionHistory.sessionId).isEqualTo(preGroupSession.id)
      assertThat(sessionHistory.sessionName).isEqualTo("Pre-group 1: Introduction")
      assertThat(sessionHistory.popName).isEqualTo("Alex River")
      assertThat(sessionHistory.groupCode).isEqualTo(groupCode)
      assertThat(sessionHistory.groupId).isEqualTo(groupId)
      assertThat(sessionHistory.attendanceStatus).isEqualTo("Attended - Complied")
    }
  }
}
