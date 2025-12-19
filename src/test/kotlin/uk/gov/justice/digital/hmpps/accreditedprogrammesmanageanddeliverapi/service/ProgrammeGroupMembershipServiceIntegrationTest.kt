package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

class ProgrammeGroupMembershipServiceIntegrationTest(@Autowired private val referralService: ReferralService) : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("allocateReferralToGroup")
  inner class AllocateReferralToGroupTests {

    @Test
    fun `can successfully allocate a referral to a group`() {
      // Given
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      // Given
      val referralFromAllocate = programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "SYSTEM",
        additionalDetails = "The additional details",
      )

      // Then
      assertThat(referralFromAllocate).isNotNull
      assertThat(referralFromAllocate.id).isEqualTo(referral.id)
      assertThat(referralFromAllocate.programmeGroupMemberships).hasSize(1)
      assertThat(referralFromAllocate.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)

      assertThat(referralFromAllocate.statusHistories).hasSize(2)
      val currentStatusHistory = referralService.getCurrentStatusHistory(referralFromAllocate)
      assertThat(currentStatusHistory!!.referralStatusDescription.id).isEqualTo(referralStatusDescriptionRepository.getScheduledStatusDescription().id)
      assertThat(currentStatusHistory.additionalDetails).isEqualTo("The additional details")
    }

    @Test
    fun `throws an error if referral does not exist`() {
      val referralId = UUID.randomUUID()
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val exception = assertThrows<NotFoundException> {
        programmeGroupMembershipService.allocateReferralToGroup(
          referralId,
          group.id!!,
          "SYSTEM",
          "",
        )
      }
      assertThat(exception.message).isEqualTo("Referral with id $referralId not found")
    }

    @Test
    fun `throws an error if group does not exist`() {
      val groupId = UUID.randomUUID()
      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      val exception = assertThrows<NotFoundException> {
        programmeGroupMembershipService.allocateReferralToGroup(
          referral.id!!,
          groupId,
          "SYSTEM",
          "",
        )
      }
      assertThat(exception.message).isEqualTo("Group with id $groupId not found")
    }

    @Test
    fun `throws an error if referral is in a closed state`() {
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      val exception = assertThrows<BusinessException> {
        programmeGroupMembershipService.allocateReferralToGroup(
          referral.id!!,
          group.id!!,
          "SYSTEM",
          "",
        )
      }
      assertThat(exception.message).isEqualTo("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    @Test
    fun `throws an error if referral already allocated to a group`() {
      val firstGroup = ProgrammeGroupFactory().withCode("AAA111").produce()
      val secondGroup = ProgrammeGroupFactory().withCode("BBB222").produce()
      testDataGenerator.createGroup(firstGroup)
      testDataGenerator.createGroup(secondGroup)

      val referral = testReferralHelper.createReferralWithStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )

      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        firstGroup.id!!,
        "SYSTEM",
        "",
      )

      val exception = assertThrows<ConflictException> {
        programmeGroupMembershipService.allocateReferralToGroup(
          referral.id!!,
          secondGroup.id!!,
          "SYSTEM",
          "",
        )
      }
      assertThat(exception.message).isEqualTo("Referral with id ${referral.id} is already allocated to a group: AAA111")
    }
  }

  @Nested
  @DisplayName("removeReferralFromGroup")
  inner class RemoveReferralFromGroup {

    var theReferral: ReferralEntity = testReferralHelper.createReferralWithStatus(
      referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
    )

    var theGroup = testDataGenerator.createGroup(
      ProgrammeGroupFactory().withCode("AAA111").produce(),
    )

    val awaitingAllocationStatusDescriptionId =
      referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id

    @BeforeEach
    fun setup() {
      theReferral = testReferralHelper.createReferralWithStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )

      theGroup = testDataGenerator.createGroup(
        ProgrammeGroupFactory().withCode("AAA111").produce(),
      )
    }

    @Test
    fun `Removes a Referral from a Group if it was already Added, and updates a Status`() {
      // Given
      programmeGroupMembershipService.allocateReferralToGroup(
        theReferral.id!!,
        theGroup.id!!,
        "THE_ALLOCATED_TO_GROUP_BY_ID",
        "any additional details",
      )

      // When
      val referralBeforeRemove = referralService.getReferralById(theReferral.id!!)
      assertThat(referralBeforeRemove.programmeGroupMemberships).hasSize(1)

      val result = programmeGroupMembershipService.removeReferralFromGroup(
        theReferral.id!!,
        theGroup.id!!,
        "THE_REMOVED_FROM_GROUP_BY_ID",
        "the additional details",
        awaitingAllocationStatusDescriptionId,
      )

      // Then
      val updatedReferral = referralService.getReferralById(theReferral.id!!)

      val currentlyAllocatedGroup = programmeGroupMembershipService.getCurrentlyAllocatedGroup(updatedReferral)
      assertThat(currentlyAllocatedGroup).isNull()

      val currentStatusHistory = referralService.getCurrentStatusHistory(updatedReferral)
      assertThat(currentStatusHistory).isNotNull
      assertThat(currentStatusHistory!!.createdBy).isEqualTo("THE_REMOVED_FROM_GROUP_BY_ID")
      assertThat(currentStatusHistory.additionalDetails).isEqualTo("the additional details")
      assertThat(currentStatusHistory.referralStatusDescription.description).isEqualTo(
        "Awaiting allocation",
      )

      // These check the implementation, but we want to make sure that we haven't got stale references or hard deletes
      assertThat(updatedReferral.programmeGroupMemberships.none { it.deletedAt === null }).isTrue
      assertThat(updatedReferral.programmeGroupMemberships).hasSize(1)
      assertThat(result.statusHistories.size).isEqualTo(updatedReferral.statusHistories.size)

      // When again
      programmeGroupMembershipService.allocateReferralToGroup(
        theReferral.id!!,
        theGroup.id!!,
        "SECOND_ALLOCATION_TO_GROUP",
        "Another set of addditional details",
      )

      programmeGroupMembershipService.removeReferralFromGroup(
        theReferral.id!!,
        theGroup.id!!,
        "REMOVED_FROM_GROUP_BY_ID",
        "the additional details",
        awaitingAllocationStatusDescriptionId,
      )
    }

    @Test
    fun `Throws an error if the Group does not exist`() {
      // Given
      val groupId = UUID.randomUUID()

      // When
      assertThrows<NotFoundException> {
        programmeGroupMembershipService.removeReferralFromGroup(
          theReferral.id!!,
          groupId,
          "",
          "",
          awaitingAllocationStatusDescriptionId,
        )
      }.message.let { message ->
        // Then
        assertThat(message).isEqualTo("Group with id $groupId not found")
      }
    }

    @Test
    fun `Throws an error if the Referral does not exist`() {
      // Given
      val referralId = UUID.randomUUID()

      // When
      assertThrows<NotFoundException> {
        programmeGroupMembershipService.removeReferralFromGroup(
          referralId,
          theGroup.id!!,
          "",
          "",
          awaitingAllocationStatusDescriptionId,
        )
      }.message.let { message ->
        // Then
        assertThat(message).isEqualTo("Referral with id $referralId not found")
      }
    }

    @Test
    fun `Throws an error if the Referral is not Assigned to the Group`() {
      // When
      assertThrows<NotFoundException> {
        programmeGroupMembershipService.removeReferralFromGroup(
          theReferral.id!!,
          theGroup.id!!,
          "",
          "",
          awaitingAllocationStatusDescriptionId,
        )
      }.message.let { message ->
        // Then
        assertThat(message).isEqualTo("No active Membership found for Referral (${theReferral.id!!}) and Group (${theGroup.id!!})")
      }
    }

    @Test
    fun `Throws an error if the proposed new ReferralStatusDescription does not exist`() {
      // When
      val nonExistentStatusDescriptionId = UUID.randomUUID()

      programmeGroupMembershipService.allocateReferralToGroup(
        theReferral.id!!,
        theGroup.id!!,
        "ANY_ALLOCATED_TO_GROUP_BY_ID",
        "any additional details",
      )
      // When
      assertThrows<NotFoundException> {
        programmeGroupMembershipService.removeReferralFromGroup(
          theReferral.id!!,
          theGroup.id!!,
          "",
          "",
          nonExistentStatusDescriptionId,
        )
      }.message.let { message ->
        // Then
        assertThat(message).isEqualTo("No Referral Status Description found for id: $nonExistentStatusDescriptionId")
      }
    }
  }
}
