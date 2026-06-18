package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDateTime

class GroupWaitlistViewRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var groupWaitListRepo: GroupWaitlistItemViewRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    // Awaiting allocation status referral
    testReferralHelper.createReferralAndUpdateStatus()
    testReferralHelper.createReferrals(3)
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `retrieve group wait list items`() {
    val waitlistItems = groupWaitListRepo.findAll()

    assertThat(waitlistItems.size).isEqualTo(1)
    assertThat(waitlistItems).allSatisfy { item ->
      assertThat(item)
        .hasFieldOrProperty("referralId")
        .hasFieldOrProperty("crn")
        .hasFieldOrProperty("personName")
        .hasFieldOrProperty("sentenceEndDate")
        .hasFieldOrProperty("sourcedFrom")
        .hasFieldOrProperty("cohort")
        .hasFieldOrProperty("hasLdc")
        .hasFieldOrProperty("dateOfBirth")
        .hasFieldOrProperty("sex")
        .hasFieldOrProperty("status")
        .hasFieldOrProperty("pduName")
        .hasFieldOrProperty("reportingTeam")
        .hasFieldOrProperty("activeProgrammeGroupId")
    }
  }

  @Test
  fun `retrieve latest active_programme_group_id`() {
    val waitlistItems = groupWaitListRepo.findAll()
    val referralId = waitlistItems.first().referralId
    val group = testGroupHelper.createGroup()
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    programmeGroupMembershipService.allocateReferralToGroup(referralId, group.id!!, "SYSTEM", "")

    val allocatedReferral = groupWaitListRepo.findByIdOrNull(referralId)!!

    assertThat(allocatedReferral.activeProgrammeGroupId).isEqualTo(group.id)
  }

  @Test
  fun `duplicate active group memberships cause materialized view refresh to fail`() {
    val referral = testReferralHelper.createReferral()

    val groupA = testGroupHelper.createGroup()
    val groupB = testGroupHelper.createGroup()

    // First active membership - inserts fine
    programmeGroupMembershipRepository.saveAndFlush(
      ProgrammeGroupMembershipEntity(
        referral = referral,
        programmeGroup = groupA,
      ),
    )

    // Second active membership for the SAME referral, deletedAt still null.
    val exception = assertThrows<DataIntegrityViolationException> {
      programmeGroupMembershipRepository.saveAndFlush(
        ProgrammeGroupMembershipEntity(
          referral = referral,
          programmeGroup = groupB,
          createdAt = LocalDateTime.now(),
          createdByUsername = "SYSTEM",
        ),
      )
    }

    assertThat(exception.message).contains("could not execute statement [ERROR: duplicate key value violates unique constraint \"idx_group_wait_list_id\"")
  }
}
