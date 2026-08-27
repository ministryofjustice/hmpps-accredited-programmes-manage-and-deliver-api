package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class ProgrammeGroupMembershipRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @BeforeEach
  override fun beforeEach() {
    super.beforeEach()
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `findByReferralAndGroupIdsIncludingDeleted returns the active membership when one exists`() {
    // Given
    val referral = testDataGenerator.createReferral("John Smith", randomCrn())
    val group = testDataGenerator.createGroup(ProgrammeGroupFactory().produce())
    testDataGenerator.createGroupMembership(
      ProgrammeGroupMembershipFactory().withReferral(referral).withProgrammeGroup(group).produce(),
    )

    // When
    val membership =
      programmeGroupMembershipRepository.findByReferralAndGroupIdsIncludingDeleted(referral.id!!, group.id!!)

    // Then
    assertThat(membership).isNotNull
    assertThat(membership!!.deletedAt).isNull()
  }

  @Test
  @Transactional
  fun `findByReferralAndGroupIdsIncludingDeleted returns a soft-deleted membership when no active one exists`() {
    // Given a PoP who was removed from the group (membership soft-deleted)
    val referral = testDataGenerator.createReferral("John Smith", randomCrn())
    val group = testDataGenerator.createGroup(ProgrammeGroupFactory().produce())
    testDataGenerator.createGroupMembership(
      ProgrammeGroupMembershipFactory()
        .withReferral(referral)
        .withProgrammeGroup(group)
        .withDeletedAt(LocalDateTime.now())
        .withDeletedByUsername("someone")
        .produce(),
    )

    // When
    val membership =
      programmeGroupMembershipRepository.findByReferralAndGroupIdsIncludingDeleted(referral.id!!, group.id!!)

    // Then the soft-deleted membership is still resolved, so attendance can be recorded
    assertThat(membership).isNotNull
    assertThat(membership!!.deletedAt).isNotNull
  }

  @Test
  @Transactional
  fun `findByReferralAndGroupIdsIncludingDeleted prefers the active membership when a deleted one also exists`() {
    // Given a PoP who was removed then re-added: one deleted and one active membership for the same group
    val referral = testDataGenerator.createReferral("John Smith", randomCrn())
    val group = testDataGenerator.createGroup(ProgrammeGroupFactory().produce())
    testDataGenerator.createGroupMembership(
      ProgrammeGroupMembershipFactory()
        .withReferral(referral)
        .withProgrammeGroup(group)
        .withCreatedAt(LocalDateTime.now().minusDays(10))
        .withDeletedAt(LocalDateTime.now().minusDays(5))
        .produce(),
    )
    testDataGenerator.createGroupMembership(
      ProgrammeGroupMembershipFactory()
        .withReferral(referral)
        .withProgrammeGroup(group)
        .withCreatedAt(LocalDateTime.now())
        .produce(),
    )

    // When
    val membership =
      programmeGroupMembershipRepository.findByReferralAndGroupIdsIncludingDeleted(referral.id!!, group.id!!)

    // Then
    assertThat(membership).isNotNull
    assertThat(membership!!.deletedAt).isNull()
  }

  @Test
  @Transactional
  fun `findByReferralAndGroupIdsIncludingDeleted returns null when no membership exists for the group`() {
    // Given
    val referral = testDataGenerator.createReferral("John Smith", randomCrn())
    val group = testDataGenerator.createGroup(ProgrammeGroupFactory().produce())

    // When
    val membership =
      programmeGroupMembershipRepository.findByReferralAndGroupIdsIncludingDeleted(referral.id!!, group.id!!)

    // Then
    assertThat(membership).isNull()
  }
}
