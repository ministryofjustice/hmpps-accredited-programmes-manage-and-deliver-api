package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

class ProgrammeGroupMembershipServiceIntegrationTest(@Autowired private val referralService: ReferralService) : IntegrationTestBase() {

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
  fun `allocateReferralToGroup can successfully allocate a referral to a group`() {
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
  fun `allocateReferralToGroup throws an error if referral does not exist`() {
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
    assertThat(exception.message).isEqualTo("No Referral found for id: $referralId")
  }

  @Test
  fun `allocateReferralToGroup throws an error if group does not exist`() {
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
  fun `allocateReferralToGroup throws an error if referral is in a closed state`() {
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
  fun `allocateReferralToGroup throws an error if referral already allocated to a group`() {
    val groupCode = "AAA111"
    val group = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(group)

    val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
    val referral = ReferralEntityFactory().produce()
    val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
    testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)
    val groupMembership = ProgrammeGroupMembershipFactory().withReferral(referral).withProgrammeGroup(group).produce()
    testDataGenerator.createGroupMembership(groupMembership)

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
}
