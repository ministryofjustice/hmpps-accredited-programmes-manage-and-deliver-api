package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService

class GroupWaitlistViewRepositoryIntegrationTest(@Autowired private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository) : IntegrationTestBase() {

  @Autowired
  private lateinit var groupWaitListRepo: GroupWaitlistItemViewRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    // Awaiting allocation status referral
    testReferralHelper.createReferralWithStatus()
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
    val group = ProgrammeGroupFactory().produce()
    testDataGenerator.createGroup(group)
    programmeGroupMembershipService.allocateReferralToGroup(referralId, group.id!!, "SYSTEM", "")

    val allocatedReferral = groupWaitListRepo.findByIdOrNull(referralId)!!

    assertThat(allocatedReferral.activeProgrammeGroupId).isEqualTo(group.id)
  }
}
