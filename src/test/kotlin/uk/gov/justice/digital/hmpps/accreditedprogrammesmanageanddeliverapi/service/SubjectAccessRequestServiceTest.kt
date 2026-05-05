package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.GroupWaitlistItemViewEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.MessageHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCaseListItemViewEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralLdcHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralMotivationBackgroundAndNonAssociationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.DeliveryLocationPreferenceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.UUID

class SubjectAccessRequestServiceTest {
  private val referralRepository = mockk<ReferralRepository>()
  private val messageHistoryRepository = mockk<MessageHistoryRepository>()
  private val attendeeRepository = mockk<AttendeeRepository>()
  private val availabilityRepository = mockk<AvailabilityRepository>()
  private val groupWaitlistItemViewRepository = mockk<GroupWaitlistItemViewRepository>()
  private val referralCaseListItemRepository = mockk<ReferralCaseListItemRepository>()
  private lateinit var service: SubjectAccessRequestService
  private lateinit var referralEntity1: ReferralEntity
  private lateinit var referralEntity2: ReferralEntity
  private lateinit var referralEntity3: ReferralEntity

  @BeforeEach
  fun setup() {
    service = SubjectAccessRequestService(
      referralRepository,
      messageHistoryRepository,
      attendeeRepository,
      availabilityRepository,
      groupWaitlistItemViewRepository,
      referralCaseListItemRepository,
    )

    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()
    val statusHistory = ReferralStatusHistoryEntityFactory()
      .produce(ReferralEntityFactory().produce(), ReferralStatusDescriptionEntityFactory().produce())
    val deliveryLocationPreferenceEntity = DeliveryLocationPreferenceEntityFactory().produce()
    val referralLdcHistoryEntity = ReferralLdcHistoryFactory().produce()
    val motivationBackgroundAndNonAssociationEntity = ReferralMotivationBackgroundAndNonAssociationsFactory()
      .produce()
    val referralReportingLocationEntity = ReferralReportingLocationFactory().produce()
    referralEntity1 = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withCreatedAt(LocalDateTime.now(UTC).minusDays(1))
      .withProgrammeGroupMemberships(mutableSetOf(programmeGroupMembershipEntity))
      .withStatusHistories(mutableListOf(statusHistory))
      .withDeliveryLocationPreferences(deliveryLocationPreferenceEntity)
      .withLdcHistories(mutableSetOf(referralLdcHistoryEntity))
      .withMotivation(motivationBackgroundAndNonAssociationEntity)
      .withReferralReportingLocationEntity(referralReportingLocationEntity)
      .produce()
    referralEntity2 = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withCreatedAt(LocalDateTime.now(UTC).minusDays(3))
      .withProgrammeGroupMemberships(mutableSetOf(programmeGroupMembershipEntity))
      .withStatusHistories(mutableListOf(statusHistory))
      .withDeliveryLocationPreferences(deliveryLocationPreferenceEntity)
      .withMotivation(motivationBackgroundAndNonAssociationEntity)
      .withReferralReportingLocationEntity(referralReportingLocationEntity)
      .produce()
    referralEntity3 = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withCreatedAt(LocalDateTime.now(UTC).plusDays(1))
      .withProgrammeGroupMemberships(mutableSetOf(programmeGroupMembershipEntity))
      .withStatusHistories(mutableListOf(statusHistory))
      .withDeliveryLocationPreferences(deliveryLocationPreferenceEntity)
      .withMotivation(motivationBackgroundAndNonAssociationEntity)
      .withReferralReportingLocationEntity(referralReportingLocationEntity)
      .produce()
  }

  @Test
  fun `should get probation content for CRN`() {
    // Given
    val crn = "X123456"
    val fromDate: LocalDate = LocalDate.now(UTC).minusDays(2)
    val toDate: LocalDate = LocalDate.now(UTC)
    val messageHistoryEntity = MessageHistoryEntityFactory()
      .withMessage("test message")
      .withDescription("test description")
      .produce()
    val sessionEntity = SessionFactory()
      .withLocationName("test session location")
      .produce()
    val attendeeEntity = AttendeeFactory()
      .withSession(sessionEntity)
      .withReferral(referralEntity1)
      .produce()
    val groupWaitlistItemViewEntity = GroupWaitlistItemViewEntityFactory().produce()
    val referralCaseListItemViewEntity = ReferralCaseListItemViewEntityFactory().produce()

    every { referralRepository.findByCrn(any()) } returns listOf(referralEntity1, referralEntity2, referralEntity3)
    every { messageHistoryRepository.findByReferral(any()) } returns listOf(messageHistoryEntity)
    every { attendeeRepository.findByReferral(any()) } returns listOf(attendeeEntity)
    every { availabilityRepository.findByReferralId(any()) } returns null
    every { groupWaitlistItemViewRepository.findByCrn(any()) } returns listOf(groupWaitlistItemViewEntity)
    every { referralCaseListItemRepository.findByCrn(any()) } returns listOf(referralCaseListItemViewEntity)

    // When
    val result = service.getProbationContentFor(crn, fromDate, toDate)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.content).isNotNull()
    val resultContent =
      result.content as SubjectAccessRequestContent

    assertThat(resultContent.referrals).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0]).isNotNull()
    assertThat(resultContent.referrals[0].id).isEqualTo(referralEntity1.id)
    assertThat(resultContent.referrals[0].crn).isEqualTo(referralEntity1.crn)
    assertThat(resultContent.referrals[0].dateOfBirth).isEqualTo(referralEntity1.dateOfBirth)
    assertThat(resultContent.referrals[0].eventId).isEqualTo(referralEntity1.eventId)
    assertThat(resultContent.referrals[0].personName).isEqualTo(referralEntity1.personName)
    assertThat(resultContent.referrals[0].sentenceEndDate).isEqualTo(referralEntity1.sentenceEndDate)
    assertThat(resultContent.referrals[0].sex).isEqualTo(referralEntity1.sex)
    assertThat(resultContent.referrals[0].cohort).isEqualTo(referralEntity1.cohort.name)
    assertThat(resultContent.referrals[0].createdAt).isEqualTo(referralEntity1.createdAt)
    assertThat(resultContent.referrals[0].interventionName).isEqualTo(referralEntity1.interventionName)
    assertThat(resultContent.referrals[0].interventionType).isEqualTo(referralEntity1.interventionType.name)
    assertThat(resultContent.referrals[0].setting).isEqualTo(referralEntity1.setting.name)
    assertThat(resultContent.referrals[0].sourcedFrom).isEqualTo(referralEntity1.sourcedFrom?.name)

    assertThat(resultContent.referrals[0].deliveryLocationPreference).isNotNull()
    assertThat(resultContent.referrals[0].deliveryLocationPreference?.createdBy)
      .isEqualTo(referralEntity1.deliveryLocationPreferences?.createdBy)
    assertThat(resultContent.referrals[0].deliveryLocationPreference?.createdAt)
      .isEqualTo(referralEntity1.deliveryLocationPreferences?.createdAt)
    assertThat(resultContent.referrals[0].deliveryLocationPreference?.lastUpdatedAt)
      .isEqualTo(referralEntity1.deliveryLocationPreferences?.lastUpdatedAt)
    assertThat(resultContent.referrals[0].deliveryLocationPreference?.locationCannotAttendText)
      .isEqualTo(referralEntity1.deliveryLocationPreferences?.locationsCannotAttendText)
    assertThat(resultContent.referrals[0].deliveryLocationPreference?.preferredDeliveryLocations)
      .hasSize(referralEntity1.deliveryLocationPreferences?.preferredDeliveryLocations?.size ?: 0)

    assertThat(resultContent.referrals[0].programmeGroupMemberships).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first()).isNotNull()
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().createdByUsername)
      .isEqualTo(referralEntity1.programmeGroupMemberships.first().createdByUsername)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().createdAt)
      .isEqualTo(referralEntity1.programmeGroupMemberships.first().createdAt)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().deletedByUsername)
      .isEqualTo(referralEntity1.programmeGroupMemberships.first().deletedByUsername)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().programmeGroup).isNotNull()
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().programmeGroup.id)
      .isEqualTo(referralEntity1.programmeGroupMemberships.first().programmeGroup.id)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().programmeGroup.accreditedProgrammeTemplate?.name)
      .isEqualTo(referralEntity1.programmeGroupMemberships.first().programmeGroup.accreditedProgrammeTemplate?.name)
    assertThat(resultContent.referrals[0].programmeGroupMemberships.first().attendances)
      .hasSize(referralEntity1.programmeGroupMemberships.first().attendances.size)

    assertThat(resultContent.referrals[0].statusHistories).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0].statusHistories[0]).isNotNull()
    assertThat(resultContent.referrals[0].statusHistories[0].additionalDetails)
      .isEqualTo(referralEntity1.statusHistories[0].additionalDetails)
    assertThat(resultContent.referrals[0].statusHistories[0].createdBy)
      .isEqualTo(referralEntity1.statusHistories[0].createdBy)
    assertThat(resultContent.referrals[0].statusHistories[0].startDate)
      .isEqualTo(referralEntity1.statusHistories[0].startDate)
    assertThat(resultContent.referrals[0].statusHistories[0].referralStatusDescription.description)
      .isEqualTo(referralEntity1.statusHistories[0].referralStatusDescription.description)

    assertThat(resultContent.referrals[0].messageHistories).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0].messageHistories[0]).isNotNull()
    assertThat(resultContent.referrals[0].messageHistories[0].description).isEqualTo(messageHistoryEntity.description)
    assertThat(resultContent.referrals[0].messageHistories[0].message).isEqualTo(messageHistoryEntity.message)

    assertThat(resultContent.referrals[0].referralLdcHistories).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0].referralLdcHistories.first()).isNotNull()
    assertThat(resultContent.referrals[0].referralLdcHistories.first().createdBy)
      .isEqualTo(referralEntity1.referralLdcHistories.first().createdBy)
    assertThat(resultContent.referrals[0].referralLdcHistories.first().createdAt)
      .isEqualTo(referralEntity1.referralLdcHistories.first().createdAt)
    assertThat(resultContent.referrals[0].referralLdcHistories.first().hasLdc)
      .isEqualTo(referralEntity1.referralLdcHistories.first().hasLdc)

    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation).isNotNull()
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.createdBy)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.createdBy)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.createdAt)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.createdAt)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.lastUpdatedBy)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.lastUpdatedBy)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.maintainsInnocence)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.maintainsInnocence)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.motivation)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.motivations)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.nonAssociation)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.nonAssociations)
    assertThat(resultContent.referrals[0].referralMotivationBackgroundAndNonAssociation?.otherConsideration)
      .isEqualTo(referralEntity1.referralMotivationBackgroundAndNonAssociations?.otherConsiderations)

    assertThat(resultContent.referrals[0].referralReportingLocation).isNotNull()
    assertThat(resultContent.referrals[0].referralReportingLocation?.regionName)
      .isEqualTo(referralEntity1.referralReportingLocationEntity?.regionName)
    assertThat(resultContent.referrals[0].referralReportingLocation?.pduName)
      .isEqualTo(referralEntity1.referralReportingLocationEntity?.pduName)
    assertThat(resultContent.referrals[0].referralReportingLocation?.reportingTeam)
      .isEqualTo(referralEntity1.referralReportingLocationEntity?.reportingTeam)

    assertThat(resultContent.referrals[0].attendees).isNotNull().hasSize(1)
    assertThat(resultContent.referrals[0].attendees[0]).isNotNull()
    assertThat(resultContent.referrals[0].attendees[0].session.createdByUsername)
      .isEqualTo(attendeeEntity.session.createdByUsername)
    assertThat(resultContent.referrals[0].attendees[0].session.endsAt)
      .isEqualTo(attendeeEntity.session.endsAt)
    assertThat(resultContent.referrals[0].attendees[0].session.locationName)
      .isEqualTo(attendeeEntity.session.locationName)
    assertThat(resultContent.referrals[0].attendees[0].session.startsAt)
      .isEqualTo(attendeeEntity.session.startsAt)

    // Module assertions
    assertThat(resultContent.referrals[0].attendees[0].session.module).isNotNull()
    assertThat(resultContent.referrals[0].attendees[0].session.module.name)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.module.name)

    // ModuleSessionTemplate assertions
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate).isNotNull()
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.description)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.description)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.durationMinutes)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.durationMinutes)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.moduleId)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.module.id)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.name)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.name)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.pathway)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.pathway.name)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.sessionNumber)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.sessionNumber)
    assertThat(resultContent.referrals[0].attendees[0].session.moduleSessionTemplate.sessionType)
      .isEqualTo(attendeeEntity.session.moduleSessionTemplate.sessionType.name)

    // SessionFacilitators assertions
    assertThat(resultContent.referrals[0].attendees[0].session.sessionFacilitators).isNotNull()
    assertThat(resultContent.referrals[0].attendees[0].session.sessionFacilitators)
      .hasSize(attendeeEntity.session.sessionFacilitators.size)

    assertThat(resultContent.groupWaitlistItemViews).isNotNull().hasSize(1)
    assertThat(resultContent.groupWaitlistItemViews[0]).isNotNull()
    assertThat(resultContent.groupWaitlistItemViews[0].crn).isEqualTo(groupWaitlistItemViewEntity.crn)
    assertThat(resultContent.groupWaitlistItemViews[0].dateOfBirth).isEqualTo(groupWaitlistItemViewEntity.dateOfBirth)
    assertThat(resultContent.groupWaitlistItemViews[0].hasLdc).isEqualTo(groupWaitlistItemViewEntity.hasLdc)
    assertThat(resultContent.groupWaitlistItemViews[0].personName).isEqualTo(groupWaitlistItemViewEntity.personName)
    assertThat(resultContent.groupWaitlistItemViews[0].referralId).isEqualTo(groupWaitlistItemViewEntity.referralId)
    assertThat(resultContent.groupWaitlistItemViews[0].sentenceEndDate)
      .isEqualTo(groupWaitlistItemViewEntity.sentenceEndDate)
    assertThat(resultContent.groupWaitlistItemViews[0].sex).isEqualTo(groupWaitlistItemViewEntity.sex)
    assertThat(resultContent.groupWaitlistItemViews[0].status).isEqualTo(groupWaitlistItemViewEntity.status)

    assertThat(resultContent.referralCaseListItemViews).isNotNull().hasSize(1)
    assertThat(resultContent.referralCaseListItemViews[0]).isNotNull()
    assertThat(resultContent.referralCaseListItemViews[0].crn).isEqualTo(groupWaitlistItemViewEntity.crn)
    assertThat(resultContent.groupWaitlistItemViews[0].hasLdc).isEqualTo(groupWaitlistItemViewEntity.hasLdc)
    assertThat(resultContent.groupWaitlistItemViews[0].personName).isEqualTo(groupWaitlistItemViewEntity.personName)
    assertThat(resultContent.groupWaitlistItemViews[0].sentenceEndDate)
      .isEqualTo(groupWaitlistItemViewEntity.sentenceEndDate)
    assertThat(resultContent.groupWaitlistItemViews[0].status).isEqualTo(groupWaitlistItemViewEntity.status)

    verify { referralRepository.findByCrn(any()) }
    verify { messageHistoryRepository.findByReferral(any()) }
    verify { attendeeRepository.findByReferral(any()) }
    verify { groupWaitlistItemViewRepository.findByCrn(any()) }
    verify { referralCaseListItemRepository.findByCrn(any()) }
  }

  @Test
  fun `should get probation content for CRN that doesn't exist`() {
    // Given
    val crn = "X123456"
    val fromDate: LocalDate = LocalDate.now(UTC).minusDays(2)
    val toDate: LocalDate = LocalDate.now(UTC)

    every { referralRepository.findByCrn(any()) } returns listOf()
    every { groupWaitlistItemViewRepository.findByCrn(any()) } returns listOf()
    every { referralCaseListItemRepository.findByCrn(any()) } returns listOf()

    // When
    val result = service.getProbationContentFor(crn, fromDate, toDate)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.content).isNotNull()
    val resultContent = result.content as SubjectAccessRequestContent
    assertThat(resultContent.referrals).isNotNull().hasSize(0)
    assertThat(resultContent.groupWaitlistItemViews).isNotNull().hasSize(0)
    assertThat(resultContent.referralCaseListItemViews).isNotNull().hasSize(0)

    verify { referralRepository.findByCrn(any()) }
    verify { groupWaitlistItemViewRepository.findByCrn(any()) }
    verify { referralCaseListItemRepository.findByCrn(any()) }
  }
}
