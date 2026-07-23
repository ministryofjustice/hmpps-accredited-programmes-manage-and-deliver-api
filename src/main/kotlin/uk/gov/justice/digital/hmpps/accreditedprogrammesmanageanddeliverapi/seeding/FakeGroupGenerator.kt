package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import net.datafaker.Faker
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import java.time.DayOfWeek
import java.time.LocalDate

@Component
@Profile("seeding")
class FakeGroupGenerator {

  fun generateCreateGroupRequest(): CreateGroupRequest {
    val groupCode = generateGroupCode()
    val faker = Faker(java.util.Random(groupCode.hashCode().toLong()))

    return CreateGroupRequest(
      groupCode = groupCode,
      cohort = ProgrammeGroupCohort.GENERAL,
      sex = ProgrammeGroupSexEnum.MALE,
      earliestStartDate = LocalDate.now().plusWeeks(2),
      createGroupSessionSlot = setOf(
        CreateGroupSessionSlot(
          dayOfWeek = DayOfWeek.MONDAY,
          hour = 10,
          minutes = 0,
          amOrPm = AmOrPm.AM,
        ),
      ),
      pduName = "Seeded PDU",
      pduCode = "SEED_PDU",
      deliveryLocationName = "Seeded Delivery Location",
      deliveryLocationCode = "SEED_LOC",
      teamMembers = listOf(
        teamMember(faker, CreateGroupTeamMemberType.TREATMENT_MANAGER),
        teamMember(faker, CreateGroupTeamMemberType.REGULAR_FACILITATOR),
      ),
    )
  }

  private fun teamMember(faker: Faker, type: CreateGroupTeamMemberType) = CreateGroupTeamMember(
    facilitator = "${faker.name().firstName()} ${faker.name().lastName()}",
    facilitatorCode = "SEEDFAC${faker.number().digits(3)}",
    teamName = "Seeded Team",
    teamCode = "SEED_TEAM",
    teamMemberType = type,
  )

  private fun generateGroupCode(): String = "Seeded" + Faker().number().digits(6)
}
