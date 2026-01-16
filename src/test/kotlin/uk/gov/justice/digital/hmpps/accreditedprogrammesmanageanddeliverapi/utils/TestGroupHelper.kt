package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.ArnsApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.GovUkApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
import java.time.LocalDate

/**
 * Test helper component for creating fully-initialised [ProgrammeGroupEntity] instances
 * in integration and service-level tests.
 *
 * <p>
 * This helper encapsulates all required external stubbing (HMPPS Auth, Gov.uk,
 * nDelius, OASys, ARNS) and delegates to [ProgrammeGroupService] to create a
 * programme group using the same code path as production.
 * </p>
 *
 * <p>
 * Intended for use in Spring Boot integration tests where realistic programme
 * group setup is required without duplicating boilerplate in individual tests.
 * </p>
 */
@TestComponent
@ExtendWith(HmppsAuthApiExtension::class)
@Import(OasysApiStubs::class, NDeliusApiStubs::class, ArnsApiStubs::class, GovUkApiStubs::class)
class TestGroupHelper {

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var govUkApiStubs: GovUkApiStubs

  @Autowired
  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  /**
   * Creates and persists a new [ProgrammeGroupEntity] using the
   * [ProgrammeGroupService], including session slots and team members.
   *
   * This method performs all required WireMock stubbing for downstream services
   * (authentication, bank holidays, and nDelius user teams) before invoking the
   * service layer.
   *
   * Unless overridden, sensible defaults are provided for all parameters to
   * minimise test setup overhead.
   *
   * @param groupCode unique programme group code; defaults to a random alphanumeric value
   * @param cohort offence cohort for the programme group
   * @param sex sex eligibility for the programme group
   * @param earliestStartDate earliest possible start date for the group
   * @param createGroupSessionSlots session slots to be created for the group
   * @param pduName probation delivery unit name
   * @param pduCode probation delivery unit code
   * @param deliveryLocationName delivery location name
   * @param deliveryLocationCode delivery location code
   * @param teamMembers team members (e.g. treatment manager, facilitators) assigned to the group
   *
   * @return the persisted [ProgrammeGroupEntity] created by the service
   */
  fun createGroup(
    groupCode: String = randomAlphanumericString(),
    cohort: ProgrammeGroupCohort = ProgrammeGroupCohort.GENERAL,
    sex: ProgrammeGroupSexEnum = ProgrammeGroupSexEnum.MALE,
    earliestStartDate: LocalDate = LocalDate.now().plusDays(1),
    createGroupSessionSlots: Set<CreateGroupSessionSlot> =
      CreateGroupSessionSlotFactory().produceUniqueSlots(count = 2),
    pduName: String = "EXAMPLE PDU",
    pduCode: String = "EXAMPLE PDU CODE",
    deliveryLocationName: String = "EXAMPLE DELIVERY LOCATION",
    deliveryLocationCode: String = "EXAMPLE DELIVERY LOCATION CODE",
    teamMembers: List<CreateGroupTeamMember> = listOf(
      CreateGroupTeamMemberFactory().produceWithRandomValues(
        teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER,
      ),
      CreateGroupTeamMemberFactory().produceWithRandomValues(
        teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR,
      ),
    ),
  ): ProgrammeGroupEntity {
    hmppsAuth.stubGrantToken()
    govUkApiStubs.stubBankHolidaysResponse()
    nDeliusApiStubs.stubUserTeamsResponse(
      "REFER_MONITOR_PP",
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

    val createGroupRequest = CreateGroupRequestFactory().produce(
      groupCode = groupCode,
      cohort = cohort,
      sex = sex,
      earliestStartDate = earliestStartDate,
      createGroupSessionSlot = createGroupSessionSlots,
      pduName = pduName,
      pduCode = pduCode,
      deliveryLocationName = deliveryLocationName,
      deliveryLocationCode = deliveryLocationCode,
      teamMembers = teamMembers,
    )

    return programmeGroupService.createGroup(createGroupRequest, "REFER_MONITOR_PP")
  }

  /**
   * Allocates a [ReferralEntity] to the given [ProgrammeGroupEntity] using the
   * [ProgrammeGroupMembershipService].
   *
   * <p>
   * This method handles all required stubbing of the nDelius API to simulate a
   * successful appointment post. It then delegates to the service layer to perform
   * the allocation, mimicking the production behaviour in an integration test.
   * </p>
   *
   * <p>
   * Typically used in test scenarios where a referral must be associated with a
   * programme group before further actions (e.g., session attendance, reporting)
   * can be tested.
   * </p>
   *
   * @param group the programme group to which the referral should be allocated
   * @param referral the referral entity to allocate; must have a non-null ID
   *
   * @return the updated [ReferralEntity] after allocation
   */
  fun allocateToGroup(
    group: ProgrammeGroupEntity,
    referral: ReferralEntity,
    additionalDetails: String = "",
  ): ReferralEntity {
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

    return programmeGroupMembershipService.allocateReferralToGroup(
      referralId = referral.id!!,
      groupId = group.id!!,
      allocatedToGroupBy = "REFER_MONITOR_PP",
      additionalDetails = additionalDetails,
    )
  }
}
