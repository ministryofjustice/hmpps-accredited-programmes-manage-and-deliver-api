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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

class ProgrammeGroupMembershipServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

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
    val result = programmeGroupMembershipService.allocateReferralToGroup(referral.id!!, group.id!!)

    // Then
    assertThat(result).isNotNull
    assertThat(result?.id).isEqualTo(referral.id)
    assertThat(result?.programmeGroupMemberships).hasSize(1)
    assertThat(result!!.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)
  }

  @Test
  fun `allocateReferralToGroup throws an error if referral does not exist`() {
    val referralId = UUID.randomUUID()
    val groupCode = "AAA111"
    val group = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(group)

    val exception = assertThrows<NotFoundException> { programmeGroupMembershipService.allocateReferralToGroup(referralId, group.id!!) }
    assertThat(exception.message).isEqualTo("Referral with id $referralId not found")
  }

  @Test
  fun `allocateReferralToGroup throws an error if group does not exist`() {
    val groupId = UUID.randomUUID()
    val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
    val referral = ReferralEntityFactory().produce()
    val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
    testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

    val exception = assertThrows<NotFoundException> { programmeGroupMembershipService.allocateReferralToGroup(referral.id!!, groupId) }
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

    val exception = assertThrows<BusinessException> { programmeGroupMembershipService.allocateReferralToGroup(referral.id!!, group.id!!) }
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

    val exception = assertThrows<BusinessException> { programmeGroupMembershipService.allocateReferralToGroup(referral.id!!, group.id!!) }
    assertThat(exception.message).isEqualTo("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
  }
}
